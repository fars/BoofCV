/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.transform.pyramid;

import gecv.alg.interpolate.InterpolatePixel;
import gecv.core.image.ConvertBufferedImage;
import gecv.factory.interpolate.FactoryInterpolation;
import gecv.gui.image.ImagePyramidPanel;
import gecv.gui.image.ShowImages;
import gecv.io.image.UtilImageIO;
import gecv.struct.image.ImageFloat32;
import gecv.struct.pyramid.ImagePyramid;
import gecv.struct.pyramid.SubsamplePyramid;

import java.awt.image.BufferedImage;

/**
 * Displays an image pyramid.
 *
 * @author Peter Abeles
 */
public class VisualizeSubsamplePyramidApp {

	public static void main( String args[] ) {
		double scales[] = new double[]{1,1.2,2.4,3.6,4.8,6.0};

		BufferedImage input = UtilImageIO.loadImage("evaluation/data/standard/boat.png");

		InterpolatePixel<ImageFloat32> interp = FactoryInterpolation.bilinearPixel(ImageFloat32.class);
//		InterpolatePixel<ImageFloat32> interp = FactoryInterpolation.bicubic(ImageFloat32.class,-0.5f);
		PyramidUpdateSubsampleScale<ImageFloat32> updater = new PyramidUpdateSubsampleScale<ImageFloat32>(interp);
		ImagePyramid<ImageFloat32> pyramid = new SubsamplePyramid<ImageFloat32>(updater,scales);

		ImageFloat32 inputF32 = ConvertBufferedImage.convertFrom(input,(ImageFloat32)null);

		pyramid.update(inputF32);

		ImagePyramidPanel<ImageFloat32> gui = new ImagePyramidPanel<ImageFloat32>(pyramid,true);
		gui.render();

		ShowImages.showWindow(gui,"Image Pyramid");
	}
}
