package com.unascribed.jxljxl.viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SegmentScope;
import java.lang.foreign.ValueLayout;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import org.imgscalr.Scalr;

import com.unascribed.jxljxl.JXLException;
import com.unascribed.jxljxl.JXLExtraChannelBuilder.JXLExtraChannelType;
import com.unascribed.jxljxl.panama.FunctionAddresses;
import com.unascribed.jxljxl.panama.JxlBasicInfo;
import com.unascribed.jxljxl.panama.JxlExtraChannelInfo;
import com.unascribed.jxljxl.panama.JxlFrameHeader;
import com.unascribed.jxljxl.panama.JxlLayerInfo;
import com.unascribed.jxljxl.panama.JxlPixelFormat;

import static com.unascribed.jxljxl.panama.LibJxl.*;

public class JXLJXLViewer {
	
	private static final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
	private static final long start = System.nanoTime();
	
	private static final SplineInterpolator overshoot = new SplineInterpolator(.68f,-0.55f,.27f,1.55f);
	
	private static final double JXL_LOGO_WIDTH = 891.6;
	private static final double JXL_LOGO_HEIGHT = 836.9;
	
	static boolean falseColor, stretch;
	record FullFrame(int x, int y, int xsize, int ysize, String name, float[] rgb, BufferedImage rgbimg, float[][] extra, BufferedImage thumb) {
		public int px() { return xsize*ysize; }
	}
	record ExtraChannel(JXLExtraChannelType type, int bitsPerSample, int exponentBitsPerSample,
			int dimShift, String name, boolean alphaPremultiplied, float[] spotColor, int cfaChannel) {}
	private static final List<FullFrame> frames = new ArrayList<>();
	private static final List<ExtraChannel> extraChannels = new ArrayList<>();
	private static int xsize, ysize;
	private static float zoom = 1;
	private static float lastAutozoom = 1;
	private static float xo, yo;
	private static final Map<float[], BufferedImage> extraChannelCache = new WeakHashMap<>();
	private static final Map<FullFrame, BufferedImage[]> rgbChannelCache = new WeakHashMap<>();
	
	static Font font;
	
	private static final MemorySegment jxl_runner = JxlThreadParallelRunnerCreate(MemorySegment.NULL, Runtime.getRuntime().availableProcessors()/2);

	public static void main(String[] args) {
		// enable a bunch of nice things that are off by default for legacy compat
		// use OpenGL if possible
		System.setProperty("sun.java2d.opengl", "true");
		// do not use DirectX, it's buggy. software is better if OGL support is missing
		System.setProperty("sun.java2d.d3d", "false");
		System.setProperty("sun.java2d.noddraw", "true");
		// force font antialiasing
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");
		System.setProperty("swing.useSystemFontSettings", "true");
		// only call invalidate as needed
		System.setProperty("java.awt.smartInvalidate", "true");
		// disable Metal's abuse of bold fonts
		System.setProperty("swing.boldMetal", "false");
		// always create native windows for popup menus (allows animations to play, etc)
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		// no ImageIO, I don't want you to write tons of tiny files to the disk, to be quite honest
		ImageIO.setUseCache(false);
		
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (Throwable t) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Throwable t2) {}
		}
		
		font = Font.decode("Dialog").deriveFont(14f);
		
		var frame = new JFrame("JXLJXL Viewer");
		
		int iconRes = 512;
		var icon = new BufferedImage(iconRes, iconRes, BufferedImage.TYPE_INT_ARGB);
		{
			var g2d = icon.createGraphics();
			fixup(g2d);
			double canvas = JXL_LOGO_WIDTH+40;
			g2d.scale(iconRes/canvas, iconRes/canvas);
			g2d.translate((canvas-JXL_LOGO_WIDTH)/2, (canvas-JXL_LOGO_HEIGHT)/2);
			g2d.setStroke(new BasicStroke(40));
			g2d.setColor(new Color(0x000000));
			drawJXLLogo(g2d, g2d::draw);
			g2d.setColor(new Color(0x5FB4B1));
			drawJXLLogo(g2d, g2d::fill);
			g2d.dispose();
		}
		
		frame.setIconImages(IntStream.of(16, 32, 64, 256, 512)
			.mapToObj(i -> icon.getScaledInstance(i, i, Image.SCALE_SMOOTH))
			.toList());
		
		var channelsList = new JList<Channel>();
		
		var channels = new JScrollPane(channelsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		channels.setBorder(title("Channels"));

		var filmstripList = new JList<FullFrame>();
		// HORIZONTAL_WRAP doesn't work no matter what VisibleRowCount is, but doing this works.
		// I dunno man whatever. it works, who cares. swing is jank
		filmstripList.setLayoutOrientation(JList.VERTICAL_WRAP);
		filmstripList.setVisibleRowCount(1);
		filmstripList.setCellRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				var f = (FullFrame)value;
				int gap = (128-f.thumb().getHeight(null))/2;
				setBorder(new EmptyBorder(gap, 8, 0, 8));
				setIcon(new ImageIcon(f.thumb()));
				setIconTextGap(gap);
				setText(index+(f.name() == null ? "" : " \""+f.name()+"\""));
				setHorizontalTextPosition(CENTER);
				setVerticalTextPosition(BOTTOM);
				setAlignmentX(0.5f);
				setHorizontalAlignment(CENTER);
				setMinimumSize(new Dimension(128, 144));
				setMaximumSize(new Dimension(192, 144));
				return this;
			}
			
		});
		
		var filmstrip = new JScrollPane(filmstripList, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		filmstrip.setBorder(title("Frames"));
		
		var checkers = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_GRAY);
		var checkersData = ((DataBufferByte)checkers.getRaster().getDataBuffer()).getData();
		for (int i = 0; i < checkersData.length; i++) {
			int o = i%16;
			if ((i/16) % 16 < 8) {
				o = 15-o;
			}
			if (o < 8) {
				checkersData[i] = (byte)0x99;
			} else {
				checkersData[i] = (byte)0x55;
			}
		}
		
		var viewer = new JComponent() {
			private long animationStart = -2_000_000_000;
			private int iteration = 0;
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				var g2d = (Graphics2D)g;
				fixup(g2d);
				
				if (!frames.isEmpty()) {
					g2d.setColor(getBackground());
					g2d.fillRect(0, 0, getWidth(), getHeight());
					
					
					g2d.translate(getWidth()/2, getHeight()/2);
					var frame = frames.get(filmstripList.getSelectedIndex());
					float z = zoom;
					if (z == 0) {
						// autofit
						float h = ((float) getHeight() / (float) ysize);
						float w = ((float) getWidth() / (float) xsize);
						float min = Math.min(h, w);
						z = min;
						lastAutozoom = z;
					}
					if (z > 1) {
						g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
					}
					g2d.scale(z, z);
					g2d.translate(-xsize/2, -ysize/2);
					g2d.translate(xo, yo);
					
					g2d.setPaint(new TexturePaint(checkers, new Rectangle2D.Float(0, 0, 16/z, 16/z)));
					g2d.fillRect(0, 0, xsize, ysize);
					
					g2d.translate(frame.x, frame.y);
					
					int idx = channelsList.getSelectedIndex();
					BufferedImage img;
					if (idx == 0) {
						img = frame.rgbimg();
					} else if (idx < 4) {
						int c = idx-1;
						var imgs = rgbChannelCache.computeIfAbsent(frame, k -> new BufferedImage[3]);
						if (imgs[c] == null) {
							imgs[c] = img = buildSingleChannelImage(frame.xsize(), frame.ysize(), frame.rgb(), 3, c);
						} else {
							img = imgs[c];
						}
					} else {
						var d = frame.extra()[idx-4];
						img = extraChannelCache.computeIfAbsent(d, k -> buildSingleChannelImage(frame.xsize(), frame.ysize(), d, 1, 0));
					}
					g2d.drawImage(img, 0, 0, null);

					g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				} else {
					g2d.setColor(getBackground());
					g2d.fillRect(0, 0, getWidth(), getHeight());
					
					double s = 0.3;
					
					float animLength = 2_000_000_000f;
					
					long n = (System.nanoTime()-start)-animationStart;
					float t = n/animLength;
					if (t < 0) t = 0;
					if (t > 1) t = 1;
					
					float r = iteration%2 == 0 ? (float)Math.PI : 0;
					
					r += (float)(overshoot.curve(t*t)*Math.PI);
					
					g2d = (Graphics2D)g.create();
					g2d.translate((getWidth()-(JXL_LOGO_WIDTH*s))/2, (getHeight()-(JXL_LOGO_HEIGHT*s))/2);
					g2d.scale(s, s);
					g2d.translate(JXL_LOGO_WIDTH/2, JXL_LOGO_HEIGHT/2);
					g2d.rotate(r);
					g2d.translate(-JXL_LOGO_WIDTH/2, -JXL_LOGO_HEIGHT/2);
					g2d.setStroke(new BasicStroke(20));
					g2d.setColor(mix(getBackground(), getForeground()));
					drawJXLLogo(g2d, g2d::draw);
					
					g2d.dispose();
					
					g2d = (Graphics2D)g;
					
					g2d.setColor(getForeground());
					g2d.setFont(font);
					
					String str = "No image is loaded. Try File → Open";
					g2d.drawString(str, (getWidth()-g2d.getFontMetrics().stringWidth(str))/2f, getHeight()-8);
					
					g2d.dispose();
					
					if (isDisplayable()) {
						if (n < animLength) {
							sched.schedule(() -> repaint(), 15, TimeUnit.MILLISECONDS);
						} else {
							iteration++;
							sched.schedule(() -> {
								animationStart = System.nanoTime()-start;
								repaint();
							}, 5, TimeUnit.SECONDS);
						}
					}
				}
			}
		};
		viewer.addMouseWheelListener(e -> {
			if (zoom == 0) {
				zoom = lastAutozoom;
			}
			zoom -= e.getPreciseWheelRotation()/20;
			if (zoom < 0.05f) zoom = 0.05f;
			viewer.repaint();
		});
		class PanListener implements MouseMotionListener, MouseListener {

			private int lastX, lastY;
			
			@Override public void mouseClicked(MouseEvent e) {}
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseMoved(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == 1) {
					lastX = e.getX();
					lastY = e.getY();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				float z = zoom == 0 ? lastAutozoom : zoom;
				xo -= (lastX-e.getX())/z;
				yo -= (lastY-e.getY())/z;
				lastX = e.getX();
				lastY = e.getY();
				viewer.repaint();
			}
			
		}
		var pan = new PanListener();
		viewer.addMouseMotionListener(pan);
		viewer.addMouseListener(pan);
		viewer.setOpaque(true);
		viewer.setBackground(channelsList.getBackground());
		var wrap = Box.createVerticalBox();
		wrap.add(viewer);
		wrap.setBorder(title("Image"));
		
		filmstripList.addListSelectionListener(e -> viewer.repaint());
		channelsList.addListSelectionListener(e -> viewer.repaint());

		var splitH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitH.add(channels);
		splitH.add(wrap);
		splitH.setDividerLocation(256);
		var splitV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		splitV.add(splitH);
		splitV.add(filmstrip);
		splitV.setDividerLocation(720-280);
		
		frame.setJMenuBar(new MenuBuilder()
			.menu("File")
				.item("Open…", KeyStroke.getKeyStroke("control O"), () -> {
					var d = new FileDialog(frame, "Open JXL file", FileDialog.LOAD);
					d.setFilenameFilter((dir, name) -> name.endsWith(".jxl"));
					d.validate();
					d.setLocationRelativeTo(frame);
					d.setVisible(true);
					if (d.getFile() == null) return;
					var f = new File(d.getDirectory(), d.getFile());
					byte[] data;
					try {
						data = Files.readAllBytes(f.toPath());
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}

					var scope = SegmentScope.auto();
					var alloc = SegmentAllocator.nativeAllocator(scope);
					var dec = JxlDecoderCreate(MemorySegment.NULL);
					try {
						if (dec == null || dec.address() == 0) throw new JXLException("Creating decoder failed");
						check(JxlDecoderSubscribeEvents(dec, JXL_DEC_BASIC_INFO() | JXL_DEC_COLOR_ENCODING() | JXL_DEC_FRAME() | JXL_DEC_BOX() | JXL_DEC_PREVIEW_IMAGE() | JXL_DEC_FULL_IMAGE()));
						
						check(JxlDecoderSetParallelRunner(dec, FunctionAddresses.JxlThreadParallelRunner$ADDR, jxl_runner));
						
						var cpy = alloc.allocate(data.length);
						cpy.copyFrom(MemorySegment.ofArray(data));
						
						JxlDecoderSetCoalescing(dec, 0);
						JxlDecoderSetInput(dec, cpy, cpy.byteSize());
						JxlDecoderSetDecompressBoxes(dec, 1);
						JxlDecoderSetRenderSpotcolors(dec, 0);
						JxlDecoderCloseInput(dec);
						
						record PartialFrame(int x, int y, int xsize, int ysize, String name) {
							public int px() { return xsize*ysize; }
						}
						
						var tmp = alloc.allocate(8);
						var info = JxlBasicInfo.allocate(alloc);
						var hdr = JxlFrameHeader.allocate(alloc);
						var fmt = JxlPixelFormat.allocate(alloc);

						MemorySegment imageout = null;
						MemorySegment extraout = null;
						List<ExtraChannel> extraChannels = new ArrayList<>();
						PartialFrame currentFrame = null;
						List<FullFrame> frames = new ArrayList<>();
						int alphaIdx = -1;
						
						while (true) {
							var event = JxlDecoderProcessInput(dec);
							
							if (event == JXL_DEC_ERROR()) {
								throw new JXLException("Decoding failed "+NO_MORE_INFO);
							} else if (event == JXL_DEC_NEED_MORE_INPUT()) {
								throw new JXLException("Decoding failed due to unexpected EOF");
							} else if (event == JXL_DEC_BASIC_INFO()) {
								check(JxlDecoderGetBasicInfo(dec, info));

								imageout = alloc.allocate(
										JxlBasicInfo.xsize$get(info)*JxlBasicInfo.ysize$get(info)*3*4
									);
								extraout = alloc.allocate(
										JxlBasicInfo.xsize$get(info)*JxlBasicInfo.ysize$get(info)*JxlBasicInfo.num_extra_channels$get(info)*4
									);
								
								var einfo = JxlExtraChannelInfo.allocate(alloc);
								
								for (int i = 0; i < JxlBasicInfo.num_extra_channels$get(info); i++) {
									check(JxlDecoderGetExtraChannelInfo(dec, i, einfo));
									int nl = JxlExtraChannelInfo.name_length$get(einfo);
									var name = nl == 0 ? null : alloc.allocate(nl+1);
									if (nl > 0) check(JxlDecoderGetExtraChannelName(dec, i, name, nl+1));
									var c = new ExtraChannel(
											JXLExtraChannelType.fromNative(JxlExtraChannelInfo.type$get(einfo)),
											JxlExtraChannelInfo.bits_per_sample$get(einfo),
											JxlExtraChannelInfo.exponent_bits_per_sample$get(einfo),
											JxlExtraChannelInfo.dim_shift$get(einfo),
											name == null ? null : name.getUtf8String(0),
											JxlExtraChannelInfo.alpha_premultiplied$get(einfo) != 0,
											JxlExtraChannelInfo.spot_color$slice(einfo).toArray(ValueLayout.JAVA_FLOAT),
											JxlExtraChannelInfo.cfa_channel$get(einfo)
									);
									if (c.type == JXLExtraChannelType.ALPHA && alphaIdx == -1) {
										alphaIdx = i;
									}
									extraChannels.add(c);
								}
							} else if (event == JXL_DEC_COLOR_ENCODING()) {
								// TODO
							} else if (event == JXL_DEC_FRAME()) {
								check(JxlDecoderGetFrameHeader(dec, hdr));
								var layer = JxlFrameHeader.layer_info$slice(hdr);
								
								int nl = JxlFrameHeader.name_length$get(hdr);
								var name = nl == 0 ? null : alloc.allocate(nl+1);
								if (nl > 0) check(JxlDecoderGetFrameName(dec, name, nl+1));
								currentFrame = new PartialFrame(
										JxlLayerInfo.crop_x0$get(layer), JxlLayerInfo.crop_y0$get(layer),
										JxlLayerInfo.xsize$get(layer), JxlLayerInfo.ysize$get(layer),
										name == null ? null : name.getUtf8String(0)
									);
							} else if (event == JXL_DEC_NEED_IMAGE_OUT_BUFFER()) {
								check(JxlDecoderGetFrameHeader(dec, hdr));

								JxlPixelFormat.num_channels$set(fmt, 3);
								JxlPixelFormat.align$set(fmt, 0);
								JxlPixelFormat.data_type$set(fmt, JXL_TYPE_FLOAT());
								JxlPixelFormat.endianness$set(fmt, JXL_NATIVE_ENDIAN());
								check(JxlDecoderSetImageOutBuffer(dec, fmt, imageout, imageout.byteSize()));
								

								JxlPixelFormat.num_channels$set(fmt, 1);
								JxlPixelFormat.align$set(fmt, 0);
								JxlPixelFormat.data_type$set(fmt, JXL_TYPE_FLOAT());
								JxlPixelFormat.endianness$set(fmt, JXL_NATIVE_ENDIAN());
								int n = currentFrame.px()*4;
								for (int i = 0; i < extraChannels.size(); i++) {
									check(JxlDecoderSetExtraChannelBuffer(dec, fmt, extraout.asSlice(i*n), n, i));
								}
								
							} else if (event == JXL_DEC_BOX()) {
								// TODO
								check(JxlDecoderGetBoxType(dec, tmp, 1));
								byte[] by = new byte[4];
								MemorySegment.ofArray(by).copyFrom(tmp.asSlice(0, 4));
							} else if (event == JXL_DEC_FULL_IMAGE()) {
								JxlDecoderFlushImage(dec);
								
								float[] rgb = new float[currentFrame.px()*3];
								MemorySegment.ofArray(rgb).copyFrom(imageout.asSlice(0, currentFrame.px()*3*4));
								
								float[][] ex = new float[extraChannels.size()][currentFrame.px()];
								int n = currentFrame.px()*4;
								for (int i = 0; i < extraChannels.size(); i++) {
									MemorySegment.ofArray(ex[i]).copyFrom(extraout.asSlice(i*n, n));
								}

								var rgbimg = new BufferedImage(currentFrame.xsize(), currentFrame.ysize(), BufferedImage.TYPE_INT_ARGB);
								int[] rgba = ((DataBufferInt)rgbimg.getRaster().getDataBuffer()).getData();
								for (int i = 0; i < currentFrame.px(); i++) {
									float r = Math.min(1, Math.max(0, rgb[(i*3)+0]));
									float g = Math.min(1, Math.max(0, rgb[(i*3)+1]));
									float b = Math.min(1, Math.max(0, rgb[(i*3)+2]));
									float a = Math.min(1, Math.max(0, alphaIdx != -1 ? ex[alphaIdx][i] : 1));
									int c = 0;
									c |= ((int)(a*255)&0xFF)<<24;
									c |= ((int)(r*255)&0xFF)<<16;
									c |= ((int)(g*255)&0xFF)<< 8;
									c |= ((int)(b*255)&0xFF)<< 0;
									rgba[i] = c;
								}
								
								BufferedImage thumb;
								
								if (rgbimg.getWidth() <= 128 && rgbimg.getHeight() <= 128) {
									thumb = rgbimg;
								} else {
									thumb = Scalr.resize(rgbimg, Scalr.Method.QUALITY, Scalr.Mode.BEST_FIT_BOTH, 128, 128);
								}
								
								frames.add(new FullFrame(currentFrame.x(), currentFrame.y(), currentFrame.xsize(), currentFrame.ysize(), currentFrame.name(),
										rgb, rgbimg, ex, thumb));
							} else if (event == JXL_DEC_PREVIEW_IMAGE()) {
								System.out.println("preview");
								// TODO
							} else if (event == JXL_DEC_SUCCESS()) {
								break;
							} else {
								throw new JXLException("Unknown event 0x"+Integer.toHexString(event)+" from decoder");
							}
						}
						lastAutozoom = 1;
						xo = yo = zoom = 0;
						xsize = JxlBasicInfo.xsize$get(info);
						ysize = JxlBasicInfo.ysize$get(info);
						JXLJXLViewer.extraChannels.clear();
						JXLJXLViewer.extraChannels.addAll(extraChannels);
						JXLJXLViewer.frames.clear();
						JXLJXLViewer.frames.addAll(frames);
						channelsList.setModel(new ChannelsModel(extraChannels));
						channelsList.setSelectedIndex(0);
						channelsList.revalidate();
						var mdl = new DefaultListModel<FullFrame>();
						mdl.addAll(frames);
						filmstripList.setModel(mdl);
						filmstripList.setSelectedIndex(0);
						filmstripList.revalidate();
						frame.repaint();
					} catch (JXLException e) {
						e.printStackTrace();
					} finally {
						JxlDecoderDestroy(dec);
					}
				})
				.item("Quit", () -> {
					frame.dispose();
					System.exit(0);
				})
				.done()
			.menu("View")
				.item("Zoom In", KeyStroke.getKeyStroke("control EQUALS"), () -> {
					if (zoom == 0) zoom = lastAutozoom;
					zoom += 0.25f;
					viewer.repaint();
				})
				.item("Zoom Out", KeyStroke.getKeyStroke("control MINUS"), () -> {
					if (zoom == 0) zoom = lastAutozoom;
					zoom -= 0.25f;
					if (zoom < 0.05f) zoom = 0.05f;
					viewer.repaint();
				})
				.item("Actual Size", KeyStroke.getKeyStroke("control 0"), () -> {
					zoom = 1;
					viewer.repaint();
				})
				.item("Fit", KeyStroke.getKeyStroke("control 1"), () -> {
					zoom = 0;
					xo = 0;
					yo = 0;
					viewer.repaint();
				})
				.separator()
				.toggleItem("False Color", b -> {
					falseColor = b;
					rgbChannelCache.clear();
					extraChannelCache.clear();
					viewer.repaint();
				})
				.toggleItem("Stretch to Full Range", b -> {
					stretch = b;
					rgbChannelCache.clear();
					extraChannelCache.clear();
					viewer.repaint();
				})
				.done()
			.buildAsMenuBar());
		
		frame.setContentPane(splitV);
		
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(960, 720);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}
	
	private static BufferedImage buildSingleChannelImage(int w, int h, float[] data, int stride, int offset) {
		float min = 0;
		float max = 1;
		if (stretch) {
			min = 1;
			max = 0;
			for (var v : data) {
				min = Math.min(min, v);
				max = Math.max(max, v);
			}
		}
		var img = new BufferedImage(w, h, falseColor ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY);
		var d = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
		for (int i = 0; i < w*h; i++) {
			float v = Math.max(0, Math.min(1, data[(i*stride)+offset]));
			if (stretch) {
				v -= min;
				v /= (max-min);
			}
			if (falseColor) {
				int vi = (int)(v*VIRIDIS.length);
				if (vi >= VIRIDIS.length) vi = VIRIDIS.length-1;
				int vc = VIRIDIS[vi];
				d[(i*3)+0] = (byte)(vc&0xFF);
				d[(i*3)+1] = (byte)((vc>>8)&0xFF);
				d[(i*3)+2] = (byte)((vc>>16)&0xFF);
			} else {
				d[i] = (byte)((int)(v*255)&0xFF);
			}
		}
		return img;
	}
	
	private static final int[] VIRIDIS = {0x000000,0x010001,0x020102,0x030103,0x030104,0x040105,0x050106,0x060207,0x070207,0x070208,0x080209,0x09030A,0x0A030B,0x0B030C,0x0B030D,0x0C040D,0x0D030E,0x0D040F,0x0E040F,0x0F0410,0x0F0511,0x100511,0x100512,0x110513,0x110513,0x120514,0x130614,0x130615,0x140615,0x140616,0x150716,0x150717,0x150717,0x160718,0x160818,0x170819,0x170819,0x18081A,0x18081A,0x18091A,0x19091B,0x19091B,0x1A091C,0x1A0A1C,0x1A0A1D,0x1B0A1D,0x1B0A1E,0x1C0B1E,0x1C0B1F,0x1C0B1F,0x1C0B20,0x1D0B20,0x1D0C20,0x1D0C21,0x1E0C21,0x1E0C22,0x1E0C22,0x1F0C23,0x1F0D23,0x200D24,0x200D24,0x200D25,0x200D25,0x210D26,0x210D26,0x210D27,0x220D27,0x220E28,0x230E28,0x230E29,0x230E29,0x240E2A,0x240E2A,0x250E2B,0x250E2B,0x250E2C,0x260E2C,0x260E2D,0x260E2D,0x270E2E,0x270E2E,0x280E2E,0x280E2F,0x280E2F,0x290E30,0x290E31,0x2A0F31,0x2A0E32,0x2A0F32,0x2B0E33,0x2B0F33,0x2C0F34,0x2C0F34,0x2C0E35,0x2D0E35,0x2D0F36,0x2D0F36,0x2E0F37,0x2E0F37,0x2F0F38,0x2F0F38,0x300F39,0x300F39,0x310F3A,0x310F3A,0x310F3B,0x320F3B,0x320F3C,0x320F3C,0x330F3D,0x330F3D,0x340F3E,0x340F3F,0x340F3F,0x350F40,0x350F40,0x360F41,0x360F41,0x360F42,0x370F42,0x370F43,0x380F43,0x380F44,0x380E44,0x390F45,0x390F46,0x3A0E46,0x3A0E47,0x3B0F47,0x3B0E47,0x3B0F48,0x3C0E49,0x3C0E49,0x3D0E4A,0x3D0E4A,0x3D0E4B,0x3E0E4B,0x3E0E4C,0x3E0E4C,0x3F0E4D,0x3F0E4D,0x400E4E,0x400E4F,0x410E4F,0x410E50,0x410E50,0x420D51,0x420E51,0x430D52,0x430D52,0x430D53,0x440D54,0x440D54,0x450D55,0x450D55,0x450D55,0x450D55,0x450D56,0x450D56,0x450D56,0x450D57,0x450D58,0x450D58,0x450E59,0x460E59,0x460E5A,0x460E5B,0x460E5B,0x460E5C,0x460F5C,0x460F5D,0x460F5E,0x460F5E,0x460F5F,0x470F5F,0x471060,0x471060,0x471061,0x471061,0x471062,0x471062,0x471163,0x471163,0x471164,0x471264,0x471265,0x471365,0x471366,0x471466,0x481567,0x481567,0x481668,0x481668,0x481769,0x481769,0x48186A,0x48186A,0x48196B,0x48196B,0x481A6C,0x481A6C,0x481B6D,0x481B6D,0x481C6E,0x481C6E,0x481D6F,0x481D6F,0x481E70,0x481E70,0x481F70,0x481F71,0x482071,0x482172,0x482172,0x482272,0x482273,0x482373,0x482374,0x482475,0x482475,0x482475,0x482576,0x482576,0x482677,0x482777,0x482777,0x482878,0x482878,0x482979,0x482979,0x472979,0x472A79,0x472A7A,0x472B7A,0x472C7A,0x472C7B,0x472D7B,0x472D7B,0x472E7C,0x472E7C,0x472F7C,0x472F7D,0x47307D,0x46307D,0x46317D,0x46317E,0x46327E,0x46327E,0x46337F,0x46337F,0x46347F,0x463480,0x463580,0x463580,0x463581,0x463681,0x453781,0x453781,0x453782,0x453882,0x453882,0x443983,0x443983,0x443A83,0x443A83,0x443A84,0x443B84,0x443B84,0x443C84,0x433C84,0x433D85,0x433D85,0x433E85,0x433E85,0x433F85,0x423F86,0x424086,0x424086,0x424186,0x424186,0x414187,0x414287,0x414287,0x414387,0x414387,0x404487,0x404488,0x404588,0x404588,0x404688,0x3F4688,0x3F4788,0x3F4788,0x3F4888,0x3F4888,0x3E4889,0x3E4989,0x3E4989,0x3E4A89,0x3E4A89,0x3E4B8A,0x3D4B8A,0x3D4C8A,0x3D4C8A,0x3D4D8A,0x3D4D8A,0x3D4E8A,0x3D4E8A,0x3D4E8A,0x3D4F8A,0x3C4F8B,0x3C508B,0x3C508B,0x3C508B,0x3C518B,0x3B518B,0x3B528B,0x3B528B,0x3B528B,0x3A538B,0x3A538B,0x3A538C,0x3A548C,0x39558C,0x39558C,0x39568C,0x39568C,0x39568C,0x39578C,0x39588C,0x38588C,0x38588C,0x38598C,0x38598C,0x375A8C,0x375A8D,0x375B8D,0x375B8D,0x365B8D,0x365C8D,0x365C8D,0x365D8D,0x365D8D,0x365D8D,0x355E8D,0x355E8D,0x355F8D,0x355F8D,0x355F8D,0x34608D,0x34608D,0x34608D,0x34618D,0x34618D,0x33628D,0x33628D,0x33628D,0x33638D,0x33638D,0x33638D,0x32648D,0x32648E,0x32658E,0x32658E,0x32668E,0x31668E,0x31668E,0x31678E,0x31678E,0x30678E,0x30688E,0x30688E,0x30698E,0x30698E,0x306A8E,0x306A8E,0x2F6A8E,0x2F6B8E,0x2F6B8E,0x2F6C8E,0x2F6C8E,0x2F6C8E,0x2F6D8E,0x2F6D8E,0x2F6D8E,0x2E6E8E,0x2E6E8E,0x2E6F8E,0x2E6F8E,0x2E6F8E,0x2E6F8E,0x2D708E,0x2D708E,0x2D708E,0x2D718E,0x2C718E,0x2C718E,0x2C728E,0x2C728E,0x2B728E,0x2B738E,0x2B738E,0x2B738E,0x2B748E,0x2B748E,0x2B758E,0x2A758E,0x2A768E,0x2A768E,0x2A768E,0x2A778E,0x2A778E,0x29788E,0x29788E,0x29788E,0x29798E,0x29798E,0x29798E,0x297A8E,0x297A8E,0x297B8E,0x287B8E,0x287C8E,0x287C8E,0x287C8E,0x287D8E,0x287D8E,0x277D8E,0x277E8E,0x277E8E,0x277F8E,0x277F8E,0x277F8E,0x27808E,0x26808E,0x26818E,0x26818E,0x26818E,0x26828E,0x25828E,0x25828E,0x25838E,0x25838E,0x25838E,0x25848E,0x25848E,0x25848E,0x25848E,0x25858E,0x25858E,0x25868E,0x25868E,0x25868E,0x25878E,0x24878E,0x24878E,0x24888E,0x24888E,0x24898E,0x23898E,0x238A8E,0x238A8E,0x238A8D,0x228B8D,0x228B8D,0x228B8D,0x228C8D,0x228C8D,0x228D8D,0x228D8D,0x218E8D,0x218E8D,0x218F8D,0x218F8D,0x21908D,0x21908D,0x21918D,0x21918C,0x21918C,0x21928C,0x20928C,0x20928C,0x20928C,0x20928C,0x20938C,0x20938C,0x20948C,0x1F948B,0x1F948B,0x1F958B,0x1F958B,0x1F968B,0x1F968B,0x1F978B,0x1F978B,0x1F978B,0x1F988B,0x1E988A,0x1E998A,0x1E998A,0x1E998A,0x1E9A8A,0x1E9A8A,0x1E9B8A,0x1E9B8A,0x1E9B8A,0x1E9C8A,0x1E9C89,0x1E9D89,0x1E9D89,0x1E9D89,0x1E9E89,0x1E9E89,0x1F9E88,0x1F9F88,0x1F9F88,0x1F9F88,0x1FA088,0x1FA088,0x1FA088,0x1FA188,0x1FA187,0x1FA187,0x1FA187,0x1FA287,0x1FA287,0x1FA287,0x20A387,0x20A386,0x20A486,0x20A486,0x20A586,0x20A585,0x21A585,0x21A685,0x21A685,0x21A685,0x21A784,0x22A884,0x22A884,0x22A884,0x22A984,0x23A984,0x23A983,0x24AA83,0x24AA83,0x24AB83,0x25AB83,0x25AB82,0x25AC82,0x26AC82,0x26AC82,0x27AC82,0x27AD81,0x27AD81,0x27AD81,0x28AE81,0x28AE80,0x28AE80,0x29AF7F,0x29AF7F,0x29B07F,0x2AB07F,0x2AB07E,0x2BB17E,0x2BB17E,0x2CB27D,0x2CB27D,0x2DB27D,0x2DB37C,0x2EB37C,0x2EB37C,0x2EB47C,0x2FB47B,0x2FB47B,0x30B57B,0x30B57A,0x31B57A,0x32B67A,0x32B679,0x33B679,0x34B779,0x34B779,0x35B778,0x35B878,0x36B878,0x37B977,0x38B977,0x38B977,0x39BA76,0x39BA76,0x3ABA76,0x3BBB75,0x3BBB75,0x3CBB75,0x3DBC74,0x3DBC74,0x3EBC73,0x3EBD73,0x3FBD72,0x40BD72,0x41BE72,0x41BE71,0x42BE71,0x42BF71,0x43BF70,0x44BF70,0x45C070,0x45C06F,0x46C06F,0x47C06E,0x48C16E,0x48C16D,0x49C26D,0x4AC26D,0x4AC26C,0x4BC26C,0x4CC36C,0x4DC36B,0x4EC36A,0x4EC46A,0x4FC46A,0x50C469,0x51C469,0x52C569,0x53C568,0x53C567,0x54C667,0x55C667,0x56C666,0x56C765,0x58C765,0x59C764,0x59C864,0x5AC863,0x5BC863,0x5CC862,0x5DC962,0x5EC961,0x5EC961,0x5FCA60,0x60CA60,0x61CA60,0x62CB5F,0x63CB5F,0x64CB5E,0x65CB5E,0x66CC5D,0x67CC5D,0x67CC5C,0x68CD5C,0x69CD5B,0x6ACD5B,0x6BCD5A,0x6CCE59,0x6DCE59,0x6DCE58,0x6FCF58,0x70CF57,0x70CF57,0x71D056,0x72D056,0x72D055,0x73D055,0x75D154,0x76D153,0x76D153,0x77D153,0x79D152,0x79D251,0x7AD251,0x7CD250,0x7DD250,0x7ED34F,0x7FD34E,0x80D34E,0x81D34D,0x82D44C,0x83D44C,0x84D44B,0x85D44A,0x86D54A,0x87D549,0x88D548,0x89D548,0x8AD647,0x8BD646,0x8CD646,0x8DD645,0x8ED744,0x8FD744,0x90D743,0x91D742,0x92D742,0x93D841,0x94D841,0x95D840,0x96D83F,0x97D83E,0x98D83E,0x99D93D,0x9BD93C,0x9CD93C,0x9DD93B,0x9EDA3A,0x9FDA3A,0x9FDA39,0xA1DA39,0xA1DA38,0xA3DB38,0xA4DB37,0xA5DB36,0xA6DB35,0xA6DB35,0xA8DC34,0xA9DC33,0xAADC33,0xABDC32,0xACDC31,0xADDD31,0xAEDD30,0xAFDD2F,0xB1DD2E,0xB2DD2E,0xB3DD2D,0xB4DE2D,0xB5DE2C,0xB6DE2B,0xB7DE2A,0xB8DE29,0xB9DE29,0xBADE28,0xBBDE27,0xBCDE27,0xBDDF26,0xBEDF25,0xBFDF24,0xC0DF24,0xC2DF23,0xC2E023,0xC4E022,0xC4E022,0xC6E021,0xC7E021,0xC8E020,0xC9E11F,0xCAE11F,0xCBE11E,0xCCE11E,0xCDE11E,0xCEE11D,0xCFE11D,0xD0E11D,0xD1E11C,0xD2E21C,0xD3E21B,0xD4E21B,0xD5E21A,0xD6E21A,0xD7E21A,0xD8E319,0xD9E319,0xDAE319,0xDBE319,0xDCE318,0xDDE318,0xDEE318,0xE0E318,0xE1E318,0xE2E418,0xE3E419,0xE4E419,0xE5E419,0xE6E419,0xE7E41A,0xE8E51A,0xE9E51A,0xEAE51A,0xEBE51B,0xECE51B,0xEDE51B,0xEEE51C,0xEFE51C,0xF0E51C,0xF1E51D,0xF2E61D,0xF3E61D,0xF4E61E,0xF4E61E,0xF6E61F,0xF6E61F,0xF7E620,0xF8E621,0xF9E621,0xFAE622,0xFAE622,0xFBE723,0xFBE724,0xFCE724,0xFCE724};
	
	private static final String NO_MORE_INFO = " (no further detail is available, try compiling libjxl with -DJXL_DEBUG_ON_ERROR)";

	private static void check(int res) throws JXLException {
		switch (res) {
			case 0x00 -> {}
			case 0x01 -> throw new JXLException("libjxl returned a generic error"+NO_MORE_INFO);
			case 0x02 -> throw new JXLException("Need more input");
			
			default -> throw new JXLException("libjxl returned unknown error 0x"+Integer.toHexString(res)+NO_MORE_INFO);
		}
	}

	private static void drawJXLLogo(Graphics2D g2d, Consumer<Shape> draw) {
		draw.accept(JXL_J);
		draw.accept(JXL_X);
		g2d.translate(JXL_LOGO_WIDTH/2, JXL_LOGO_HEIGHT/2);
		g2d.rotate(Math.PI);
		g2d.translate(-JXL_LOGO_WIDTH/2, -JXL_LOGO_HEIGHT/2);
		draw.accept(JXL_J);
	}

	private static void fixup(Graphics2D g2d) {
		// Bring Java2D up to the present
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		// With this set to DEFAULT, stroke geometry will be sloppily approximated; especially visible on animated arcs
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
	}

	private static Border title(String str) {
		return new TitledBorder(new EmptyBorder(0, 0, 0, 0), str, 0, 0, font);
	}

	private static Color mix(Color a, Color b) {
		int red = (a.getRed()+b.getRed())/2;
		int green = (a.getGreen()+b.getGreen())/2;
		int blue = (a.getBlue()+b.getBlue())/2;
		return new Color(red, green, blue, a.getAlpha());
	}
	
	private static final Path2D.Float JXL_J = new PathBuilder()
			.m(153,625.6)
			.c(8.3714,34.29967, 5.10613,69.06107, -11,91.2)
			.c(-6.2,8, -14.8,14.5, -25.6,19.3)
			.L(53.9,836.9)
			.c(36.9,0, 69.4,-5.8, 96.5,-17.4)
			.c(64.83052,-27.99572, 97.16406,-87.08522, 97,-157.2)
			.c(0,-24.8, -4,-44.6, -5.7,-52)
			.L(200.8,337)
			.c(0,0, 0.1,-60.18006, 0.1,-90.2)
			.H(0)
			.V(337)
			.h(109.8)
			.Z()
			.build();
	private static final Path2D.Float JXL_X = new PathBuilder()
			.M(495.7,420.6)
			.L(607.4,203.7)
			.H(503.8)
			.L(431.7,343.6)
			.L(315.3,203.7)
			.H(211.7)
			.L(392,420.6)
			.L(282.5,633.1)
			.h(103.6)
			.l(69.8,-135.5)
			.l(112.7,135.5)
			.h(103.6)
			.Z()
			.build();

}
