/*
 * Copyright (c) 2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://www.boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.feature.detect.extract;

import boofcv.abst.feature.detect.extract.FeatureExtractor;
import boofcv.abst.feature.detect.extract.GeneralFeatureDetector;
import boofcv.abst.feature.detect.intensity.GeneralFeatureIntensity;
import boofcv.abst.filter.derivative.AnyImageDerivative;
import boofcv.alg.misc.PixelMath;
import boofcv.alg.transform.gss.UtilScaleSpace;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.core.image.inst.FactoryImageGenerator;
import boofcv.factory.feature.detect.extract.FactoryFeatureExtractor;
import boofcv.factory.feature.detect.intensity.FactoryGeneralIntensity;
import boofcv.gui.ProcessInput;
import boofcv.gui.SelectAlgorithmImagePanel;
import boofcv.gui.feature.FancyInterestPointRender;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.gui.image.VisualizeImageData;
import boofcv.io.image.ImageListManager;
import boofcv.struct.QueueCorner;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageFloat32;
import georegression.struct.point.Point2D_I16;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Displays a window showing the selected corner-laplace features across different scale spaces.
 *
 * @author Peter Abeles
 */
public class CompareFeatureExtractorApp<T extends ImageBase, D extends ImageBase>
		extends SelectAlgorithmImagePanel implements ProcessInput , GeneralExtractConfigPanel.Listener
{
	T grayImage;

	Class<T> imageType;

	GeneralExtractConfigPanel configPanel;

	boolean processImage = false;
	BufferedImage input;
	BufferedImage intensityImage;
	BufferedImage workImage;
	AnyImageDerivative<T,D> deriv;

	GeneralFeatureIntensity<T,D> intensityAlg;
	int minSeparation = 5;

	// which image is being viewed in the GUI
	int viewImage = 2;

	int radius = 2;
	int numFeatures = 200;
	float thresholdFraction = 0.1f;

	FancyInterestPointRender render = new FancyInterestPointRender();

	ImagePanel imagePanel;

	public CompareFeatureExtractorApp(Class<T> imageType, Class<D> derivType) {
		super(1);
		this.imageType = imageType;

		addAlgorithm(0, "Harris", FactoryGeneralIntensity.harris(radius,0.04f,imageType));
		addAlgorithm(0, "KLT", FactoryGeneralIntensity.klt(radius, derivType));
		addAlgorithm(0, "FAST", FactoryGeneralIntensity.fast(5, 11, derivType));
		addAlgorithm(0, "KitRos", FactoryGeneralIntensity.kitros(derivType));

		deriv = UtilScaleSpace.createDerivatives(imageType, FactoryImageGenerator.create(derivType));

		JPanel gui = new JPanel();
		gui.setLayout(new BorderLayout());

		imagePanel = new ImagePanel();

		this.configPanel = new GeneralExtractConfigPanel();
		configPanel.setThreshold(thresholdFraction);
		configPanel.setFeatureSeparation(minSeparation);
		configPanel.setImageIndex(viewImage);
		configPanel.setListener(this);

		gui.add(configPanel,BorderLayout.WEST);
		gui.add(imagePanel,BorderLayout.CENTER);

		setMainGUI(gui);
	}

	public void process( BufferedImage input ) {
		this.input = input;
		grayImage = ConvertBufferedImage.convertFrom(input,null,imageType);
		workImage = new BufferedImage(input.getWidth(),input.getHeight(),BufferedImage.TYPE_INT_BGR);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				doRefreshAll();
			}});
	}

	@Override
	public void refreshAll(Object[] cookies) {
		setActiveAlgorithm(0,null,cookies[0]);
	}

	@Override
	public void setActiveAlgorithm(int indexFamily, String name, Object cookie) {
		if( input == null )
			return;

		intensityAlg = (GeneralFeatureIntensity<T,D>)cookie;

		doProcess();
	}

	private synchronized void doProcess() {
		System.out.println("radius "+radius+" min separation "+minSeparation+" thresholdFraction "+thresholdFraction+" numFeatures "+numFeatures);

		deriv.setInput(grayImage);
		D derivX = deriv.getDerivative(true);
		D derivY = deriv.getDerivative(false);
		D derivXX = deriv.getDerivative(true,true);
		D derivYY = deriv.getDerivative(false,false);
		D derivXY = deriv.getDerivative(true,false);

		// todo modifying buffered images which might be actively being displayed

		intensityAlg.process(grayImage, derivX, derivY, derivXX, derivYY, derivXY);
		ImageFloat32 intensity = intensityAlg.getIntensity();
		intensityImage = VisualizeImageData.colorizeSign(intensityAlg.getIntensity(), null, PixelMath.maxAbs(intensity));

		float max = PixelMath.maxAbs(intensity);
		float threshold = max*thresholdFraction;

		FeatureExtractor extractor = FactoryFeatureExtractor.nonmax(minSeparation, threshold, 0);
		GeneralFeatureDetector<T,D> detector = new GeneralFeatureDetector<T,D>(intensityAlg,extractor,numFeatures);
		detector.process(grayImage,derivX,derivY,derivXX,derivYY,derivXY);
		QueueCorner foundCorners = detector.getFeatures();

		render.reset();

		for( int i = 0; i < foundCorners.size(); i++ ) {
			Point2D_I16 p = foundCorners.get(i);
			render.addPoint(p.x,p.y,3, Color.RED);
		}

		Graphics2D g2 = workImage.createGraphics();
		g2.drawImage(input,0,0,grayImage.width,grayImage.height,null);
		render.draw(g2);
		drawImage();

	}

	@Override
	public void changeImage(String name, int index) {
		ImageListManager manager = getInputManager();

		BufferedImage image = manager.loadImage(index);
		if( image != null ) {
			process(image);
		}
	}

	@Override
	public boolean getHasProcessedImage() {
		return processImage;
	}

	@Override
	public void changeImage(int index) {
		this.viewImage = index;
		drawImage();
	}

	private void drawImage() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				switch( viewImage ) {
					case 0:
						imagePanel.setBufferedImage(input);
						break;

					case 1:
						imagePanel.setBufferedImage(intensityImage);
						break;

					case 2:
						imagePanel.setBufferedImage(workImage);
						break;
				}
				BufferedImage b = imagePanel.getImage();
				imagePanel.setPreferredSize(new Dimension(b.getWidth(),b.getHeight()));
				imagePanel.repaint();

				processImage = true;
			}});
	}

	@Override
	public synchronized void changeFeatureSeparation(int radius) {
		minSeparation = radius;
		doProcess();
	}

	@Override
	public synchronized void changeThreshold(double value) {
		this.thresholdFraction = (float)value;
		doProcess();
	}

	@Override
	public synchronized void changeNumFeatures(int total) {
		this.numFeatures = total;
		doProcess();
	}

	public static void main( String args[] ) {
		CompareFeatureExtractorApp app = new CompareFeatureExtractorApp(ImageFloat32.class,ImageFloat32.class);

		ImageListManager manager = new ImageListManager();
		manager.add("shapes","data/shapes01.png");
		manager.add("sunflowers","data/sunflowers.png");
		manager.add("beach","data/scale/beach02.jpg");

		app.setInputManager(manager);

		// wait for it to process one image so that the size isn't all screwed up
		while( !app.getHasProcessedImage() ) {
			Thread.yield();
		}

		ShowImages.showWindow(app,"Feature Extraction");

		System.out.println("Done");
	}
}