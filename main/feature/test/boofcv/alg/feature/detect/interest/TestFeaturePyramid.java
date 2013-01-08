/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
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

package boofcv.alg.feature.detect.interest;

import boofcv.abst.filter.derivative.AnyImageDerivative;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.transform.gss.ScaleSpacePyramid;
import boofcv.core.image.inst.FactoryImageGenerator;
import boofcv.struct.image.ImageFloat32;


/**
 * @author Peter Abeles
 */
public class TestFeaturePyramid extends GenericFeatureScaleDetector {

	@Override
	protected Object createDetector(GeneralFeatureDetector<ImageFloat32, ImageFloat32> detector) {
		AnyImageDerivative<ImageFloat32, ImageFloat32> deriv = GImageDerivativeOps.createDerivatives(ImageFloat32.class, FactoryImageGenerator.create(ImageFloat32.class));

		return new FeaturePyramid<ImageFloat32, ImageFloat32>(detector, deriv, 1);
	}

	@Override
	protected int detectFeature(ImageFloat32 input, Object detector) {
		ScaleSpacePyramid<ImageFloat32> ss = new ScaleSpacePyramid<ImageFloat32>(ImageFloat32.class, new double[]{1,2,4,8,16});
		ss.setImage(input);

		FeaturePyramid<ImageFloat32, ImageFloat32> alg =
				(FeaturePyramid<ImageFloat32, ImageFloat32>) detector;
		alg.detect(ss);

		return alg.getInterestPoints().size();
	}

}

