#pragma once
#ifndef RECOGNIZE_H_
#define RECOGNIZE_H_
#include <string>
//#include "net.h"
#include "ncnn-20201218-android/arm64-v8a/include/ncnn/mat.h"
#include "ncnn-20201218-android/arm64-v8a/include/ncnn/net.h"
#include <algorithm>
#include <math.h>
#define round(x) ( (int)(x + 0.5) )
namespace Face {
	class Recognize {
	public:
		Recognize(const std::string &model_path);
		~Recognize();
		void start(ncnn::Mat& ncnn_img, std::vector<float>&feature128);
		void SetThreadNum(int threadNum);
	private:
		void RecogNet(ncnn::Mat& img_);
		ncnn::Net Recognet;
		//ncnn::Mat ncnn_img;
		std::vector<float> feature_out;
		int threadnum = 1;
	};

	double calculSimilar(std::vector<float>& v1, std::vector<float>& v2);
}


#endif // !RECOGNIZE_H_