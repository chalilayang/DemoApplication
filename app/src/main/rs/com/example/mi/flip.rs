#pragma version(1)
#pragma rs java_package_name(com.example.mi)

rs_allocation gIn;
rs_allocation gOut;
uint32_t imageWidth;
uint32_t imageHeight;

uchar4 RS_KERNEL flip(const uchar4 in, uint32_t x, uint32_t y) {
    uchar4 fliped = in;
    if (y < imageHeight / 3) {
        fliped = rsGetElementAt_uchar4(gIn, x, imageHeight - y);
    } else {
        fliped = rsGetElementAt_uchar4(gIn, x, y);
    }
    return fliped;
}


void flip_setup(rs_allocation alocationIn, rs_allocation alocationOut){
    imageWidth = rsAllocationGetDimX(alocationIn);
    imageHeight = rsAllocationGetDimY(alocationIn);
    gIn = alocationIn;
    gOut = alocationOut;
}
