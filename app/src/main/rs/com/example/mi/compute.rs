#pragma version(1)
#pragma rs java_package_name(com.example.mi)

int param = 3;

// 转成长度
float RS_KERNEL toLength(float4 in, uint32_t x, uint32_t y) {
  return length(in);
}

// 将长度拆到二维坐标系中
float2 RS_KERNEL to3float(float in, uint32_t x, uint32_t y) {
  float base = sqrt(in);
  return (float2){base, base * (float)param};
}

void compute(rs_allocation input, rs_allocation output, int fromX, int toX, int fromY, int toY) {
    rsDebug("fromX", fromX);
    rsDebug("toX", toX);
    rsDebug("fromY", fromY);
    rsDebug("toY", toY);
    rsDebug("param", param);
    param = 4;

    // 获取输入的宽度和高度
    const uint32_t width = rsAllocationGetDimX(input);
    const uint32_t height = rsAllocationGetDimY(input);
    // 临时结构用于存储中间数据
    rs_allocation tmp = rsCreateAllocation_float(width, height);
    rsForEach(toLength, input, tmp);
    rsForEach(to3float, tmp, output);
}