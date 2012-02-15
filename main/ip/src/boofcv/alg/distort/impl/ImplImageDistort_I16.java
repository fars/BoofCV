/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.distort.impl;

import boofcv.alg.interpolate.InterpolatePixel;
import boofcv.core.image.border.ImageBorder;
import boofcv.core.image.border.ImageBorder_I32;
import boofcv.struct.distort.ImageDistort;
import boofcv.struct.distort.PixelTransform_F32;
import boofcv.struct.image.ImageInt16;
import boofcv.struct.image.ImageInteger;


/**
 * <p>Implementation of {@link boofcv.struct.distort.ImageDistort}.</p>
 *
 * <p>
 * DO NOT MODIFY: Generated by {@link boofcv.alg.distort.impl.GeneratorImplImageDistort}.
 * </p>
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"UnnecessaryLocalVariable"})
public class ImplImageDistort_I16<T extends ImageInt16> implements ImageDistort<T> {

	// transform from dst to src image
	private PixelTransform_F32 dstToSrc;
	// sub pixel interpolation
	private InterpolatePixel<T> interp;
	// handle the image border
	private ImageBorder_I32 border;

	// crop boundary
	int x0,y0,x1,y1;

	public ImplImageDistort_I16(PixelTransform_F32 dstToSrc, InterpolatePixel<T> interp , ImageBorder<ImageInteger> border ) {
		this.dstToSrc = dstToSrc;
		this.interp = interp;
		this.border = (ImageBorder_I32)border;
	}

	@Override
	public void setModel(PixelTransform_F32 dstToSrc) {
		this.dstToSrc = dstToSrc;
	}

	@Override
	public void apply( T srcImg , T dstImg ) {
		interp.setImage(srcImg);

		x0 = 0;y0 = 0;x1 = dstImg.width;y1 = dstImg.height;

		if( border != null )
			applyBorder(srcImg, dstImg);
		else
			applyNoBorder(srcImg, dstImg);
	}

	@Override
	public void apply( T srcImg , T dstImg , int dstX0, int dstY0, int dstX1, int dstY1 ) {
		interp.setImage(srcImg);

		x0 = dstX0;y0 = dstY0;x1 = dstX1;y1 = dstY1;

		if( border != null )
			applyBorder(srcImg, dstImg);
		else
			applyNoBorder(srcImg, dstImg);
	}

	public void applyBorder( T srcImg , T dstImg ) {

		border.setImage(srcImg);

		final float minInterpX = interp.getUnsafeBorderX();
		final float minInterpY = interp.getUnsafeBorderY();
		final float maxInterpX = srcImg.getWidth()-interp.getUnsafeBorderX();
		final float maxInterpY = srcImg.getHeight()-interp.getUnsafeBorderY();

		final float widthF = srcImg.getWidth();
		final float heightF = srcImg.getHeight();

		for( int y = y0; y < y1; y++ ) {
			int indexDst = dstImg.startIndex + dstImg.stride*y + x0;
			for( int x = x0; x < x1; x++ , indexDst++ ) {
				dstToSrc.compute(x,y);

				final float sx = dstToSrc.distX;
				final float sy = dstToSrc.distY;

				if( sx < minInterpX || sx >= maxInterpX || sy < minInterpY || sy >= maxInterpY ) {
					if( sx >= 0f && sx < widthF && sy >= 0f && sy < heightF )
						dstImg.data[indexDst] = (short)interp.get(sx,sy);
					else
						dstImg.data[indexDst] = (short)border.getOutside((int)sx,(int)sy);
				} else {
					dstImg.data[indexDst] = (short)interp.get_unsafe(sx,sy);
				}
			}
		}
	}

	public void applyNoBorder( T srcImg , T dstImg ) {

		final float minInterpX = interp.getUnsafeBorderX();
		final float minInterpY = interp.getUnsafeBorderY();
		final float maxInterpX = srcImg.getWidth()-interp.getUnsafeBorderX();
		final float maxInterpY = srcImg.getHeight()-interp.getUnsafeBorderY();

		final float widthF = srcImg.getWidth();
		final float heightF = srcImg.getHeight();

		for( int y = y0; y < y1; y++ ) {
			int indexDst = dstImg.startIndex + dstImg.stride*y + x0;
			for( int x = x0; x < x1; x++ , indexDst++ ) {
				dstToSrc.compute(x,y);

				final float sx = dstToSrc.distX;
				final float sy = dstToSrc.distY;

				if( sx < minInterpX || sx >= maxInterpX || sy < minInterpY || sy >= maxInterpY ) {
					if( sx >= 0f && sx < widthF && sy >= 0f && sy < heightF )
						dstImg.data[indexDst] = (short)interp.get(sx,sy);
				} else {
					dstImg.data[indexDst] = (short)interp.get_unsafe(sx,sy);
				}
			}
		}
	}

}
