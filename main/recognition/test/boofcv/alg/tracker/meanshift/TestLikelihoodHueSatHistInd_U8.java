package boofcv.alg.tracker.meanshift;

import boofcv.alg.color.ColorHsv;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;
import georegression.struct.shapes.Rectangle2D_I32;
import org.junit.Test;

import static boofcv.alg.tracker.meanshift.TestLikelihoodHistCoupled_U8.setColor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestLikelihoodHueSatHistInd_U8 {

	@Test
	public void numBins() {
		LikelihoodHueSatHistInd_U8 alg = new LikelihoodHueSatHistInd_U8(255,30);

		MultiSpectral<ImageUInt8> image = new MultiSpectral<ImageUInt8>(ImageUInt8.class,30,40,3);

		// make sure the upper limit is handled correctly
		setColor(image,5,6,255,255,255);
		alg.setImage(image);
		alg.createModel(new Rectangle2D_I32(5,6,1,1));

		assertEquals(30 , alg.binsH.length);
		assertEquals(30 , alg.binsS.length);

		// it comes out to a slightly larger size on purpose
		assertEquals(2*Math.PI,alg.sizeH*30,0.01);
		assertEquals(1.0,alg.sizeS*30,0.01);
	}

	@Test
	public void convertToHueSat() {
		LikelihoodHueSatHistInd_U8 alg = new LikelihoodHueSatHistInd_U8(255,30);

		MultiSpectral<ImageUInt8> image = new MultiSpectral<ImageUInt8>(ImageUInt8.class,30,40,3);
		setColor(image,5,6,120,50,255);
		alg.setImage(image);
		alg.createModel(new Rectangle2D_I32(5,6,1,1));

		float hsv[] = new float[3];
		ColorHsv.rgbToHsv(120, 50, 255, hsv);

		int indexH = (int)(hsv[0]/alg.sizeH);
		int indexS = (int)(hsv[1]/alg.sizeS);

		assertEquals(1.0,alg.binsH[indexH],1e-4);
		assertEquals(1.0,alg.binsS[indexS],1e-4);
	}

	@Test
	public void singleColor() {
		LikelihoodHueSatHistInd_U8 alg = new LikelihoodHueSatHistInd_U8(255,5);

		MultiSpectral<ImageUInt8> image = new MultiSpectral<ImageUInt8>(ImageUInt8.class,30,40,3);

		Rectangle2D_I32 r = new Rectangle2D_I32(3,4,12,8);
		setColor(image,r,100,105,12);

		alg.setImage(image);
		alg.createModel(r);

		assertEquals(1.0f,alg.compute(3, 4),1e-4);
		assertEquals(1.0f,alg.compute(14, 11),1e-4);
		assertEquals(0,alg.compute(10, 30),1e-4);
	}

	@Test
	public void multipleColors() {
		LikelihoodHueSatHistInd_U8 alg = new LikelihoodHueSatHistInd_U8(255,5);

		MultiSpectral<ImageUInt8> image = new MultiSpectral<ImageUInt8>(ImageUInt8.class,30,40,3);

		Rectangle2D_I32 r0 = new Rectangle2D_I32(3,4,8,8);
		Rectangle2D_I32 r1 = new Rectangle2D_I32(11,4,4,8);
		setColor(image,r0,100,105,12);
		setColor(image,r1,50,200,50);


		Rectangle2D_I32 region = new Rectangle2D_I32(3,4,12,8);
		alg.setImage(image);
		alg.createModel(region);


		float v0 = alg.compute(3, 4);
		float v1 = alg.compute(11, 4);

		assertTrue(v0>v1);
	}

}
