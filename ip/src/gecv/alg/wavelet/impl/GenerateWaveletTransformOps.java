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

package gecv.alg.wavelet.impl;

import gecv.misc.CodeGeneratorBase;
import gecv.misc.CodeGeneratorUtil;
import gecv.misc.TypeImage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


/**
 * @author Peter Abeles
 */
public class GenerateWaveletTransformOps extends CodeGeneratorBase  {
	String className = "WaveletTransformOps";

	PrintStream out;

	TypeImage imageIn;
	TypeImage imageOut;
	String genName;
	String sumType;
	String bitWise;
	String outputCast;

	public GenerateWaveletTransformOps() throws FileNotFoundException {
		out = new PrintStream(new FileOutputStream(className + ".java"));
	}

	@Override
	public void generate() throws FileNotFoundException {
		printPreamble();

		printFuncs(TypeImage.F32,TypeImage.F32);
		printFuncs(TypeImage.S32,TypeImage.S32);

		out.print("\n" +
				"}\n");
	}

	private void printPreamble() {
		out.print(CodeGeneratorUtil.copyright);
		out.print("package gecv.alg.wavelet;\n" +
				"\n" +
				"import gecv.alg.InputSanityCheck;\n" +
				"import gecv.alg.wavelet.impl.ImplWaveletTransformBorder;\n" +
				"import gecv.alg.wavelet.impl.ImplWaveletTransformInner;\n" +
				"import gecv.alg.wavelet.impl.ImplWaveletTransformNaive;\n" +
				"import gecv.struct.image.ImageFloat32;\n" +
				"import gecv.struct.image.ImageSInt32;\n" +
				"import gecv.struct.image.ImageUInt8;\n" +
				"import gecv.struct.wavelet.WaveletDescription;\n" +
				"import gecv.struct.wavelet.WlCoef_F32;\n" +
				"import gecv.struct.wavelet.WlCoef_I32;\n" +
				"\n" +
				"/**\n" +
				" * <p>\n" +
				" * Functional interface for applying general purpose wavelet and inverse wavelet transforms.\n" +
				" * </p>\n" +
				" *\n" +
				" * <p>\n" +
				" * A single level wavelet transform breaks the image up into four regions:\n" +
				" * <table border=\"1\">\n" +
				" * <tr><td>a</td><td>h</td></tr>\n" +
				" * <tr><td>v</td><td>d</td></tr>\n" +
				" * </table>\n" +
				" * Each region has M/2,N/2 rows and columns. Region 'a' is the scaling image, 'h' and 'v' are\n" +
				" * a combination of scaling and wavelet, and 'd' is a combination of horizontal and vertical wavelets.\n" +
				" * When a multiple level transform is performed then the input to the next level is the 'a' from the previous\n" +
				" * level.\n" +
				" * </p>\n" +
				" *\n" +
				" * <p>\n" +
				" * DO NOT MODIFY: This class was automatically generated by {@link gecv.alg.wavelet.impl.Generate"+className+"}\n" +
				" * </p>\n" +
				" *\n" +
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class "+className+" {\n\n");
	}

	private void printFuncs( TypeImage imageIn , TypeImage imageOut ) {
		this.imageIn = imageIn;
		this.imageOut = imageOut;

		if( imageIn.isInteger() )
			genName = "I32";
		else
			genName = "F"+imageIn.getNumBits();

		sumType = imageIn.getSumType();
		bitWise = imageIn.getBitWise();

		if( sumType.compareTo(imageOut.getDataType()) == 0 ) {
			outputCast = "";
		} else {
			outputCast = "("+imageOut.getDataType()+")";
		}

		printTransform1();
		printTransformN();
		printInvert1();
		printInvertN();
	}

	private void printTransform1() {
		out.print("\t/**\n" +
				"\t * <p>\n" +
				"\t * Performs a single level wavelet transform.\n" +
				"\t * </p>\n" +
				"\t *\n" +
				"\t * @param desc Description of the wavelet.\n" +
				"\t * @param input Input image. Not modified.\n" +
				"\t * @param output Where the wavelet transform is written to. Modified.\n" +
				"\t * @param storage Optional storage image.  Should be the same size as output image. If null then\n" +
				"\t * an image is declared internally.\n" +
				"\t */\n" +
				"\tpublic static void transform1( WaveletDescription<WlCoef_"+genName+"> desc ,\n" +
				"\t\t\t\t\t\t\t\t   "+imageIn.getImageName()+" input , "+imageOut.getImageName()+" output ,\n" +
				"\t\t\t\t\t\t\t\t   "+imageOut.getImageName()+" storage )\n" +
				"\t{\n" +
				"\t\tUtilWavelet.checkShape(input,output);\n" +
				"\n" +
				"\t\tWlCoef_"+genName+" coef = desc.getForward();\n" +
				"\n" +
				"\t\tif( output.width < coef.scaling.length || output.width < coef.wavelet.length )\n" +
				"\t\t\tthrow new IllegalArgumentException(\"Wavelet is too large for provided image.\");\n" +
				"\t\tif( output.height < coef.scaling.length || output.height < coef.wavelet.length )\n" +
				"\t\t\tthrow new IllegalArgumentException(\"Wavelet is too large for provided image.\");\n" +
				"\t\tstorage = InputSanityCheck.checkDeclare(output, storage);\n" +
				"\n" +
				"\t\t// the faster routines can only be run on images which are not too small\n" +
				"\t\tint minSize = Math.max(coef.getScalingLength(),coef.getWaveletLength())*3;\n" +
				"\n" +
				"\t\tif( input.getWidth() <= minSize || input.getHeight() <= minSize ) {\n" +
				"\t\t\tImplWaveletTransformNaive.horizontal(desc.getBorder(),coef,input,storage);\n" +
				"\t\t\tImplWaveletTransformNaive.vertical(desc.getBorder(),coef,storage,output);\n" +
				"\t\t} else {\n" +
				"\t\t\tImplWaveletTransformInner.horizontal(coef,input,storage);\n" +
				"\t\t\tImplWaveletTransformBorder.horizontal(desc.getBorder(),coef,input,storage);\n" +
				"\t\t\tImplWaveletTransformInner.vertical(coef,storage,output);\n" +
				"\t\t\tImplWaveletTransformBorder.vertical(desc.getBorder(),coef,storage,output);\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	private void printTransformN() {
		out.print("\t/**\n" +
				"\t * <p>\n" +
				"\t * Performs a level N wavelet transform using the fast wavelet transform (FWT).\n" +
				"\t * </p>\n" +
				"\t *\n" +
				"\t * <p>To save memory the input image is used to store intermediate results and is modified.</p>\n" +
				"\t *\n" +
				"\t * @param desc Description of the wavelet.\n" +
				"\t * @param input Input image and is used as internal workspace. Modified.\n" +
				"\t * @param output Where the multilevel wavelet transform is written to. Modified.\n" +
				"\t * @param storage Optional storage image.  Should be the same size as output image. If null then\n" +
				"\t * an image is declared internally.\n" +
				"\t * @param numLevels Number of levels which should be computed in the transform.\n" +
				"\t */\n" +
				"\tpublic static void transformN( WaveletDescription<WlCoef_"+genName+"> desc ,\n" +
				"\t\t\t\t\t\t\t\t   "+imageIn.getImageName()+" input , "+imageOut.getImageName()+" output ,\n" +
				"\t\t\t\t\t\t\t\t   "+imageOut.getImageName()+" storage ,\n" +
				"\t\t\t\t\t\t\t\t   int numLevels )\n" +
				"\t{\n" +
				"\t\tif( numLevels == 1 ) {\n" +
				"\t\t\ttransform1(desc,input,output, storage);\n" +
				"\t\t\treturn;\n" +
				"\t\t}\n" +
				"\n" +
				"\t\tUtilWavelet.checkShape(desc.getForward(),input,output,numLevels);\n" +
				"\t\tstorage = InputSanityCheck.checkDeclare(output, storage);\n" +
				"\t\t// modify the shape of a temporary image not the original\n" +
				"\t\tstorage = storage.subimage(0,0,output.width,output.height);\n" +
				"\n" +
				"\t\ttransform1(desc,input,output, storage);\n" +
				"\n" +
				"\t\tfor( int i = 2; i <= numLevels; i++ ) {\n" +
				"\t\t\tint width = output.width/2;\n" +
				"\t\t\tint height = output.height/2;\n" +
				"\t\t\twidth += width%2;\n" +
				"\t\t\theight += height%2;\n" +
				"\n" +
				"\t\t\tinput = input.subimage(0,0,width,height);\n" +
				"\t\t\toutput = output.subimage(0,0,width,height);\n" +
				"\t\t\tinput.setTo(output);\n" +
				"\n" +
				"\t\t\t// transform the scaling image and save the results in the output image\n" +
				"\t\t\tstorage.reshape(width,height);\n" +
				"\t\t\ttransform1(desc,input,output,storage);\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	private void printInvert1() {
		out.print("\t/**\n" +
				"\t * <p>\n" +
				"\t * Performs a single level inverse wavelet transform. Do not pass in a whole image which has been\n" +
				"\t * transformed by a multilevel transform.  Just the relevant sub-image.\n" +
				"\t * </p>\n" +
				"\t *\n" +
				"\t * @param desc Description of the inverse wavelet.\n" +
				"\t * @param input Input wavelet transform. Not modified.\n" +
				"\t * @param output Reconstruction of original image. Modified.\n" +
				"\t * @param storage Optional storage image.  Should be the same size as the input image. If null then\n" +
				"\t * an image is declared internally.\n" +
				"\t */\n" +
				"\tpublic static void inverse1( WaveletDescription<WlCoef_"+genName+"> desc ,\n" +
				"\t\t\t\t\t\t\t\t "+imageOut.getImageName()+" input , "+imageIn.getImageName()+" output ,\n" +
				"\t\t\t\t\t\t\t\t "+imageIn.getImageName()+" storage )\n" +
				"\t{\n" +
				"\t\tUtilWavelet.checkShape(output,input);\n" +
				"\t\tWlCoef_"+genName+" coef = desc.getForward();\n" +
				"\t\tif( output.width < coef.scaling.length || output.width < coef.wavelet.length )\n" +
				"\t\t\tthrow new IllegalArgumentException(\"Wavelet is too large for provided image.\");\n" +
				"\t\tif( output.height < coef.scaling.length || output.height < coef.wavelet.length )\n" +
				"\t\t\tthrow new IllegalArgumentException(\"Wavelet is too large for provided image.\");\n" +
				"\t\tstorage = InputSanityCheck.checkDeclare(input, storage);\n" +
				"\n" +
				"\t\t// the faster routines can only be run on images which are not too small\n" +
				"\t\tint minSize = Math.max(coef.getScalingLength(),coef.getWaveletLength())*3;\n" +
				"\n" +
				"\t\tif( output.getWidth() <= minSize || output.getHeight() <= minSize ) {\n" +
				"\t\t\tImplWaveletTransformNaive.verticalInverse(desc.getBorder(),desc.getInverse(),input,storage);\n" +
				"\t\t\tImplWaveletTransformNaive.horizontalInverse(desc.getBorder(),desc.getInverse(),storage,output);\n" +
				"\t\t} else {\n" +
				"\t\t\tImplWaveletTransformInner.verticalInverse(desc.getInverse().getInnerCoefficients(),input,storage);\n" +
				"\t\t\tImplWaveletTransformBorder.verticalInverse(desc.getBorder(),desc.getInverse(),input,storage);\n" +
				"\t\t\tImplWaveletTransformInner.horizontalInverse(desc.getInverse().getInnerCoefficients(),storage,output);\n" +
				"\t\t\tImplWaveletTransformBorder.horizontalInverse(desc.getBorder(),desc.getInverse(),storage,output);\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	private void printInvertN() {
		out.print("\t/**\n" +
				"\t * <p>Performs a level N inverse fast wavelet transform (FWT).</p>\n" +
				"\t *\n" +
				"\t * <p>To save memory the input image is used to store intermediate results and is modified.</p>\n" +
				"\t *\n" +
				"\t * @param desc Description of the inverse wavelet.\n" +
				"\t * @param input Input wavelet transform and is used as internal workspace. Modified.\n" +
				"\t * @param output Reconstruction of original image. Modified.\n" +
				"\t * @param storage Optional storage image.  Should be the same size as the input image. If null then\n" +
				"\t * an image is declared internally.\n" +
				"\t * @param numLevels Number of levels in the transform.\n" +
				"\t */\n" +
				"\tpublic static void inverseN( WaveletDescription<WlCoef_"+genName+"> desc ,\n" +
				"\t\t\t\t\t\t\t\t "+imageOut.getImageName()+" input , "+imageIn.getImageName()+" output ,\n" +
				"\t\t\t\t\t\t\t\t "+imageOut.getImageName()+" storage,\n" +
				"\t\t\t\t\t\t\t\t int numLevels )\n" +
				"\t{\n" +
				"\t\tif( numLevels == 1 ) {\n" +
				"\t\t\tinverse1(desc,input,output, storage);\n" +
				"\t\t\treturn;\n" +
				"\t\t}\n" +
				"\n" +
				"\t\tUtilWavelet.checkShape(desc.getForward(),output,input,numLevels);\n" +
				"\t\tstorage = InputSanityCheck.checkDeclare(input, storage);\n" +
				"\t\t// modify the shape of a temporary image not the original\n" +
				"\t\tstorage = storage.subimage(0,0,input.width,input.height);\n" +
				"\n" +
				"\t\tint width,height;\n" +
				"\n" +
				"\t\tint scale = UtilWavelet.computeScale(numLevels);\n" +
				"\t\twidth = input.width/scale;\n" +
				"\t\theight = input.height/scale;\n" +
				"\t\twidth += width%2;\n" +
				"\t\theight += height%2;\n" +
				"\n" +
				"\t\t"+imageOut.getImageName()+" levelIn = input.subimage(0,0,width,height);\n" +
				"\t\t"+imageIn.getImageName()+" levelOut = output.subimage(0,0,width,height);\n" +
				"\t\tstorage.reshape(width,height);\n" +
				"\t\tinverse1(desc,levelIn,levelOut, storage);\n" +
				"\n" +
				"\t\tfor( int i = numLevels-1; i >= 1; i-- ) {\n" +
				"\t\t\t// copy the decoded segment into the input\n" +
				"\t\t\tlevelIn.setTo(levelOut);\n" +
				"\t\t\tif( i > 1 ) {\n" +
				"\t\t\t\tscale /= 2;\n" +
				"\t\t\t\twidth = input.width/scale;\n" +
				"\t\t\t\theight = input.height/scale;\n" +
				"\t\t\t\twidth += width%2;\n" +
				"\t\t\t\theight += height%2;\n" +
				"\n" +
				"\t\t\t\tstorage.reshape(width,height);\n" +
				"\t\t\t\tlevelIn = input.subimage(0,0,width,height);\n" +
				"\t\t\t\tlevelOut = output.subimage(0,0,width,height);\n" +
				"\t\t\t} else {\n" +
				"\t\t\t\tlevelIn = input;\n" +
				"\t\t\t\tlevelOut = output;\n" +
				"\t\t\t}\n" +
				"\n" +
				"\t\t\tstorage.reshape(levelIn.width,levelIn.height);\n" +
				"\t\t\tinverse1(desc,levelIn,levelOut, storage);\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public static void main( String args[] ) throws FileNotFoundException {
		GenerateWaveletTransformOps app = new GenerateWaveletTransformOps();
		app.generate();
	}
}
