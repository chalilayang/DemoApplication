#pragma version(1)
#pragma rs java_package_name(com.example.mi)

rs_allocation gIn;
rs_allocation gOut;
uint32_t imageWidth;
uint32_t imageHeight;

uchar4 RS_KERNEL flip(const uchar4 in, uint32_t x, uint32_t y) {
    uchar4 fliped = in;
    if (y < imageHeight / 2) {
        fliped = rsGetElementAt_uchar4(gIn, x, y);
    } else {
        fliped = in;
    }
    return fliped;
}

uchar4 RS_KERNEL addPixel(const uchar4 in, uint32_t x, uint32_t y) {
    uchar4 fliped = rsGetElementAt_uchar4(gIn, x, imageHeight - y);
    uchar4 result = fliped;
    result.r = in.r + (fliped.r - in.r) / 2;
    result.g = in.g + (fliped.g - in.g) / 2;
    result.b = in.b + (fliped.b - in.b) / 2;
    return result;
}

void flip_setup(rs_allocation alocationIn, rs_allocation alocationOut){
    imageWidth = rsAllocationGetDimX(alocationIn);
    imageHeight = rsAllocationGetDimY(alocationIn);
    gIn = alocationIn;
    gOut = alocationOut;
}

static const float4 weight = {0.299f, 0.587f, 0.114f, 0.0f};

uchar4 RS_KERNEL invert(uchar4 in, uint32_t x, uint32_t y) {
    uchar4 out = in;
    out.r = 255 - in.r;
    out.g = 255 - in.g;
    out.b = 255 - in.b;
    return out;
}

uchar4 RS_KERNEL greyscale(uchar4 in) {
    const float4 inF = rsUnpackColor8888(in);
    const float val = dot(inF, weight);
    const float4 outF = (float4){val, val, val, val};
    return rsPackColorTo8888(inF);
}

void process(rs_allocation inputImage, rs_allocation outputImage) {
    const uint32_t imageWidth = rsAllocationGetDimX(inputImage);
    const uint32_t imageHeight = rsAllocationGetDimY(inputImage);
    rs_allocation tmp = rsCreateAllocation_uchar4(imageWidth, imageHeight);
    rsForEach(invert, inputImage, tmp);
    rsForEach(greyscale, tmp, outputImage);
}

void process2(rs_allocation inputImage, rs_allocation inputExtraImage, rs_allocation outputImage) {
    imageWidth = rsAllocationGetDimX(inputImage);
    imageHeight = rsAllocationGetDimY(inputImage);
    gIn = inputExtraImage;
    rsForEach(addPixel, inputImage, outputImage);
}
