/*
 * MIT License
 *
 * Copyright (c) 2023 Una Thompson (unascribed)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
				int m = VIRIDIS.length/3;
				float vm = v*m;
				float t = vm%1;
				int i1 = (int)Math.floor(vm);
				if (i1 >= m) i1--;
				int i2 = (int)Math.ceil(vm);
				if (i2 >= m) i2--;
				
				float r1 = (VIRIDIS[(i1*3)+0]&0xFF)/255f;
				float g1 = (VIRIDIS[(i1*3)+1]&0xFF)/255f;
				float b1 = (VIRIDIS[(i1*3)+2]&0xFF)/255f;
				
				float r2 = (VIRIDIS[(i2*3)+0]&0xFF)/255f;
				float g2 = (VIRIDIS[(i2*3)+1]&0xFF)/255f;
				float b2 = (VIRIDIS[(i2*3)+2]&0xFF)/255f;
				
				byte r = (byte)((int)(lerp(r1, r2, t)*255)&0xFF);
				byte g = (byte)((int)(lerp(g1, g2, t)*255)&0xFF);
				byte b = (byte)((int)(lerp(b1, b2, t)*255)&0xFF);
				
				d[(i*3)+0] = b;
				d[(i*3)+1] = g;
				d[(i*3)+2] = r;
			} else {
				d[i] = (byte)((int)(v*255)&0xFF);
			}
		}
		return img;
	}
	
	private static float lerp(float a, float b, float t) {
		return a*(1-t)+b*t;
	}
	
	private static final byte[] VIRIDIS = {(byte)4,(byte)1,(byte)4,(byte)12,(byte)3,(byte)13,(byte)18,(byte)5,(byte)19,(byte)23,(byte)8,(byte)24,(byte)27,(byte)10,(byte)29,(byte)30,(byte)12,(byte)34,(byte)33,(byte)13,(byte)39,(byte)37,(byte)14,(byte)44,(byte)41,(byte)14,(byte)48,(byte)45,(byte)15,(byte)54,(byte)49,(byte)15,(byte)59,(byte)53,(byte)15,(byte)64,(byte)57,(byte)15,(byte)69,(byte)61,(byte)14,(byte)75,(byte)65,(byte)14,(byte)80,(byte)69,(byte)13,(byte)85,(byte)70,(byte)14,(byte)90,(byte)71,(byte)15,(byte)96,(byte)71,(byte)18,(byte)101,(byte)72,(byte)24,(byte)106,(byte)72,(byte)29,(byte)110,(byte)72,(byte)34,(byte)115,(byte)72,(byte)39,(byte)119,(byte)71,(byte)44,(byte)123,(byte)70,(byte)49,(byte)126,(byte)70,(byte)54,(byte)129,(byte)68,(byte)58,(byte)131,(byte)67,(byte)63,(byte)133,(byte)65,(byte)68,(byte)135,(byte)62,(byte)72,(byte)136,(byte)61,(byte)77,(byte)138,(byte)59,(byte)81,(byte)139,(byte)57,(byte)85,(byte)140,(byte)55,(byte)90,(byte)141,(byte)53,(byte)94,(byte)141,(byte)51,(byte)98,(byte)141,(byte)49,(byte)102,(byte)142,(byte)48,(byte)106,(byte)142,(byte)47,(byte)110,(byte)142,(byte)45,(byte)113,(byte)142,(byte)43,(byte)117,(byte)142,(byte)41,(byte)121,(byte)142,(byte)40,(byte)125,(byte)142,(byte)38,(byte)128,(byte)142,(byte)37,(byte)132,(byte)142,(byte)36,(byte)135,(byte)142,(byte)34,(byte)139,(byte)141,(byte)33,(byte)144,(byte)141,(byte)32,(byte)147,(byte)140,(byte)31,(byte)151,(byte)139,(byte)30,(byte)155,(byte)138,(byte)31,(byte)159,(byte)136,(byte)31,(byte)162,(byte)135,(byte)33,(byte)166,(byte)133,(byte)36,(byte)170,(byte)131,(byte)39,(byte)173,(byte)129,(byte)43,(byte)177,(byte)126,(byte)48,(byte)181,(byte)123,(byte)54,(byte)184,(byte)120,(byte)60,(byte)188,(byte)116,(byte)67,(byte)191,(byte)113,(byte)74,(byte)194,(byte)108,(byte)82,(byte)197,(byte)104,(byte)91,(byte)200,(byte)99,(byte)99,(byte)203,(byte)94,(byte)108,(byte)206,(byte)89,(byte)117,(byte)209,(byte)84,(byte)127,(byte)211,(byte)78,(byte)138,(byte)214,(byte)71,(byte)148,(byte)216,(byte)65,(byte)158,(byte)218,(byte)58,(byte)168,(byte)220,(byte)52,(byte)179,(byte)222,(byte)45,(byte)190,(byte)223,(byte)38,(byte)200,(byte)225,(byte)32,(byte)211,(byte)226,(byte)28,(byte)221,(byte)227,(byte)24,(byte)232,(byte)229,(byte)26,(byte)241,(byte)229,(byte)29,(byte)250,(byte)230,(byte)34};
	
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
