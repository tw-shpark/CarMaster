/*
=======
>>>>>>> origin/utis
#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT void JNICALL Java_com_teleworks_carmaster_CarmasterVideoFragment_VideoProcessing(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);

JNIEXPORT void JNICALL Java_com_teleworks_carmaster_CarmasterVideoFragment_VideoProcessing(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> v;

    FastFeatureDetector detector(50);
    detector.detect(mGr, v);
    for( unsigned int i = 0; i < v.size(); i++ )
    {
        const KeyPoint& kp = v[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }
}
}
<<<<<<< HEAD
*/



#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv/cv.h>
#include <opencv/cxcore.h>
#include <opencv/highgui.h>
#include <opencv/cvaux.h>

#include <vector>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>

#include <android/log.h> // Android LOG

#include "lane.h"
#include "LaneDetect.h"

#define  LOG_TAG    "TOPVIEW"
#define  LOGUNK(...)  __android_log_print(ANDROID_LOG_UNKNOWN,LOG_TAG,__VA_ARGS__)
#define  LOGDEF(...)  __android_log_print(ANDROID_LOG_DEFAULT,LOG_TAG,__VA_ARGS__)
#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGF(...)  __android_log_print(ANDROID_FATAL_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGS(...)  __android_log_print(ANDROID_SILENT_ERROR,LOG_TAG,__VA_ARGS__)

#define rear_cut 31*24+9*24
#define left_cut 31*24+6*24+12
#define right_cut 31*24+4*24+7
#define i_st 1
#define i_ed 1280
#define j_st 1
#define rx_st 240
#define ry_st 45
#define ry_ed 675
#define center 640

#define WIDTH 640
#define HEIGHT 480

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT void JNICALL Java_com_teleworks_carmaster_CarmasterVideoFragment_VideoProcessing(JNIEnv*, jobject, jlong addrGray, jlong addrRgba, jlong flags);

static int start_flag = 1;

int LaneRecognition(IplImage *frame_buf, IplImage *gray, int max_lane_number)
{
	PointKAIST left[10], right[10];
	int cur_lane = 0;
	int det_lane = 0;
	int temp_time = 0;
	int mode = 0;
	int result = 0;
	int i=0, j=0;
	char fps_str[5][256];
	CvFont font;
	clock_t start_time, end_time;
	start_time = clock();

	set_number_of_lane(max_lane_number);

	for(i=0; i<10; i++)
	{
		left[i].x = 0;
		left[i].y = 0;
		right[i].x = 0;
		right[i].y = 0;
	}

	result = LDWS((unsigned char*)gray->imageData, WIDTH, HEIGHT, left, right);
	det_lane = DoLaneDetect(frame_buf, mode, start_flag);
	start_flag = 0;

	mode = 0;

	if(det_lane != 0)
	{
		cur_lane = max_score();
	}
	else if(result == 1)
	{
		if(temp_time > 150)
		{
			//shift_right();
			mode = 1;
			temp_time = 0;
		}

		cur_lane = max_score();
	}
	else if(result == 2)
	{
		if(temp_time > 150)
		{
			//shift_left();
			mode = 1;
			temp_time = 0;
		}

		cur_lane = max_score();

	}
	else
	{
		cur_lane = max_score();
	}


	temp_time++;

	if(temp_time > 10000)
		temp_time = 0;

	end_time = clock();

	sprintf(fps_str[0], "Lane: 1, %.2f%%", get_likelihood(1)*100);
	sprintf(fps_str[1], "Lane: 2, %.2f%%", get_likelihood(2)*100);
	sprintf(fps_str[2], "Lane: 3, %.2f%%", get_likelihood(3)*100);
	sprintf(fps_str[3], "Lane: 4, %.2f%%", get_likelihood(4)*100);

	sprintf(fps_str[4], "Computation Time : %.2f ms", (float)(end_time-start_time)/CLOCKS_PER_SEC * 1000);

	cvInitFont(&font, CV_FONT_HERSHEY_SIMPLEX,0.6,0.7,0,2,8);

	for(i=0; i<5; i++)
	{
		if(cur_lane-1 == i)
			cvPutText(frame_buf,fps_str[i], cvPoint(5, 20 + i*25),&font, CV_RGB(255,0,0));
		else
			cvPutText(frame_buf,fps_str[i], cvPoint(5, 20 + i*25),&font, CV_RGB(0,255,0));
	}

	return cur_lane;
}

double ClipY(double PixelValue){
	if(PixelValue<16.0)
		PixelValue = 16.0;
	else if(PixelValue >235.0)
		PixelValue = 235.0;
	return PixelValue;
}
double ClipBR(double PixelValue){
	if(PixelValue<16.0)
		PixelValue = 16.0;
	else if(PixelValue >240.0)
		PixelValue = 240.0;
	return PixelValue;
}
double line2point(int x1,int y1, int line_x1, int line_y1, int line_x2, int line_y2)
{
	double distance=0,a1=0,b1=0;

	if(line_x2*1.0-line_x1*1.0 != 0)
		a1 = ((line_y2*1.0-line_y1*1.0)/(line_x2*1.0-line_x1*1.0));
	else
		a1 = 10000;

	if(line_x2*1.0-line_x1*1.0 != 0)
		b1 = (line_y1*line_x2*1.0-line_y2*1.0*line_x1*1.0)/(line_x2*1.0-line_x1*1.0);
	else
		b1 = 10000;

	if(a1*a1+1 != 0)
		distance = abs(-1*a1*x1+y1-b1)/sqrt(a1*a1+1);
	else
		distance = 10000;

	return distance;
}
void controlrgb(uchar &rear_r,uchar &rear_g,uchar &rear_b)
{
	double r,g,b;
	r = rear_r;
	g = rear_g;
	b = rear_b;
	double Y;
	double Cb;
	double Cr;

	Y = (0.299*r+0.587*g+0.114*b);
	Cb = ((-0.169*r-0.331*g+0.5*b)+0.5);
	Cr = ((0.5*r-0.419*g-0.081*b)+0.5);

	Y = Y*1.4;

	r = floor((Y+1.402*Cr)*0.98);
	if(r>255){r = 255;}
	if(r<0){r = 0;}
	g = floor(Y+(-0.334)*Cb+(-0.713)*Cr);
	if(g>255){g = 255;}
	if(g<0){g = 0;}
	b = floor(Y+1.772*Cb);
	if(b>255){b = 255;}
	if(b<0){b = 0;}

	rear_r = (uchar)r;
	rear_g = (uchar)g;
	rear_b = (uchar)b;
}
void rgb2Y(double &rear_r,double &rear_g,double &rear_b,double &Y)
{
	double r,g,b;
	r = rear_r;
	g = rear_g;
	b = rear_b;

	Y = (0.299*r+0.587*g+0.114*b);
}
void controlrgb1(uchar &rear_r,uchar &rear_g,uchar &rear_b, double &k)
{
	double r,g,b;
	r = rear_r;
	g = rear_g;
	b = rear_b;
	double Y;
	double Cb;
	double Cr;

	Y = ClipY(0.257*r+0.504*g+0.098*b+16);
	Cb = ClipBR((-0.148*r-0.291*g+0.439*b)+128);
	Cr = ClipBR((0.439*r-0.368*g-0.071*b)+128);


	Y = Y*k;

	r = floor((Y+1.402*(Cr-128))+0.5);
	if(r>255){r = 255;}
	if(r<0){r = 0;}
	g = floor(Y+(-0.334414)*(Cb-128)+(-0.71414)*(Cr-128)+0.5);
	if(g>255){g = 255;}
	if(g<0){g = 0;}
	b = floor(Y+1.772*(Cb-128)+0.5);
	if(b>255){b = 255;}
	if(b<0){b = 0;}

	rear_r = (uchar)r;
	rear_g = (uchar)g;
	rear_b = (uchar)b;
}
void controlrgb2(uchar &rear_r,uchar &rear_g,uchar &rear_b, double &cr, double &cg, double &cb)
{
	double r,g,b;
	r = rear_r;
	g = rear_g;
	b = rear_b;

	r = r*cr;
	r = floor(r);
	if(r>255){r = 255;}
	if(r<0){r = 0;}
	g = g*cg;
	g = floor(g);
	if(g>255){g = 255;}
	if(g<0){g = 0;}
	b = b*cb;
	b = floor(b);
	if(b>255){b = 255;}
	if(b<0){b = 0;}

	rear_r = (uchar)r;
	rear_g = (uchar)g;
	rear_b = (uchar)b;
}

// img : rear, img2 : left, img3 : right
void Topview(IplImage *img, IplImage *img2, IplImage *img3, IplImage *undist_img)
{
	//cv::Mat left_image(800,1280,CV_8UC1,cv::Scalar(0));
	//cv::Mat right_image(800,1280,CV_8UC1,cv::Scalar(0));
	//cv::Mat rear_image(800,1280,CV_8UC1,cv::Scalar(0));
/*
	cv::Mat Inter_right(800,1280,CV_8UC1,cv::Scalar(0));
	cv::Mat Inter_left(800,1280,CV_8UC1,cv::Scalar(0));
	cv::Mat FMM_left(800,1280,CV_8UC1,cv::Scalar(0));
	cv::Mat FMM_right(800,1280,CV_8UC1,cv::Scalar(0));
*/
	int w,h,w2,h2,i,j,ii,jj,w3,h3,w4,h4;
	int xx,yy;
	int table[2][900][1290]={{{-1,},},};
	int table_left[2][900][1290]={{{-1,},},};
	int table_right[2][900][1290]={{{-1,},},};
	int flagtable[900][1290]={{-1,},};

	uchar left_image[800][1280]={0}, right_image[800][1280]={0}, rear_image[800][1280]={0};
	uchar Inter_right[800][1280]={0}, Inter_left[800][1280]={0}, FMM_left[800][1280]={0}, FMM_right[800][1280]={0};

	double matrix_left[3][3]={{-1.415706, -0.989851, 1934.359253},{-0.223215, -1.181200, 938.850647},{0.000247, -0.001393, 1.000000}}; //1占쏙옙
	double matrix_right[3][3]={{0.863149, -0.810486, 260.209137},{-0.320579, 0.140184, 217.276901},{-0.000307, -0.000954, 1.000000}}; //1占쏙옙
	double matrix[3][3]={{1.733896, -0.920175, -458.782867 },{-0.010799, 0.260302, 65.995651},{0.000019, -0.001456, 1.000000}};//1占쏙옙 占쏙옙占쏙옙

	//printf("1");
	for(i=0;i<900;i++)
	{
		for(j=0;j<1290;j++)
		{
			table[0][i][j]=table[1][i][j]=-1;
			table_left[0][i][j]=table_left[1][i][j]=-1;
			table_right[0][i][j]=table_right[1][i][j]=-1;
			flagtable[i][j]=-1;
		}
	}

	char c;
	double cc;
//	printf("1");
	//IplImage *carimg;
//	printf("adsfsaasdfaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaafd");

	w=img->width;
	h=img->height;
	CvSize a,a2;
	h2=800;
	w2=1300;
//	printf("%d %d\n",w2,h2);
//	printf("%d %d\n",w,h);
	a.height=h2;
	a.width=w2;
//	printf("1\n");
	//carimg=cvLoadImage("car.bmp",1);
	//cvShowImage("carimage",carimg);
	//h4=carimg->height;
	//w4=carimg->width;
	h3=h2;
	w3=w2;
//	printf("asdf      %d    adsf   %d\n",h2, w2);
	//IplImage *undist_img=cvCreateImage(a,IPL_DEPTH_8U,3);
	//IplImage *print_img=cvCreateImage(a2,IPL_DEPTH_8U,3);
	///video properties
	//double fps=cvGetCaptureProperty(capture,CV_CAP_PROP_FPS);
	CvSize frame_size=a;

///////////


//	printf("1\n");


	int count;
	for(j=0;j<h2;j++) //i,j占쏙옙 占쏙옙占� 占싱뱄옙占쏙옙占쏙옙 占쏙옙표
	{
		count=0;
		for(i=i_st;i<i_ed;i++)
		{
			cc=matrix[2][0]*(i)	+(matrix[2][1])*(j)	+matrix[2][2];
			xx=(int)(((((double)(i))*(matrix[0][0])	+((double)(j))*(matrix[0][1]))	+(matrix[0][2]))/cc);
			yy=(int)(((((double)(i))*(matrix[1][0])	+((double)(j))*(matrix[1][1]))	+(matrix[1][2]))/cc);//xx,yy占쏙옙 占쏙옙 占싱뱄옙占쏙옙占쏙옙 占쏙옙표.
			if(xx>=w||xx<0||yy>=h||yy<0)
			{
				table[0][j][i]=-1;
				table[1][j][i]=-1;
				continue;
			}
			else if(xx<rx_st*1.2*(ry_ed-yy)/ry_ed-20||xx>i_ed-rx_st*1.2*(ry_ed-yy)/ry_ed+20||yy>ry_ed||yy<ry_st) // distortion占쌍댐옙 占싸븝옙 占쏙옙占쏙옙 占싹깍옙.
			{
				table[0][j][i]=-1;
				table[1][j][i]=-1;
			}
			else
			{
				table[0][j][i]=xx;
				table[1][j][i]=yy;
			}
		}
	}

	//printf("1\n");
	for(j=j_st;j<h2;j++) //i,j占쏙옙 占쏙옙占� 占싱뱄옙占쏙옙占쏙옙 占쏙옙표
	{
		for(i=center;i<i_ed;i++)
		{
			cc=matrix_left[2][0]*(i)	+(matrix_left[2][1])*(j)	+matrix_left[2][2];
			xx=(int)(((((double)(i))*(matrix_left[0][0])	+((double)(j))*(matrix_left[0][1]))	+(matrix_left[0][2]))/cc);
			yy=(int)(((((double)(i))*(matrix_left[1][0])	+((double)(j))*(matrix_left[1][1]))	+(matrix_left[1][2]))/cc);//xx,yy占쏙옙 占쏙옙 占싱뱄옙占쏙옙占쏙옙 占쏙옙표.


			if(xx>=w||xx<0||yy>=h||yy<0)
			{
				table_left[0][j][i]=-1;
				table_left[1][j][i]=-1;
				continue;
			}
			else
			{
				table_left[0][j][i]=xx;
				table_left[1][j][i]=yy;
			}
		}
	}

	for(j=j_st;j<h2;j++) //i,j占쏙옙 占쏙옙占� 占싱뱄옙占쏙옙占쏙옙 占쏙옙표
	{
		for(i=i_st;i<center;i++)
		{
			cc=matrix_right[2][0]*i	+(matrix_right[2][1])*j	+matrix_right[2][2];
			xx=(int)(((((double)i)*(matrix_right[0][0])	+((double)j)*(matrix_right[0][1]))	+(matrix_right[0][2]))/cc);
			yy=(int)(((((double)i)*(matrix_right[1][0])	+((double)j)*(matrix_right[1][1]))	+(matrix_right[1][2]))/cc);//xx,yy占쏙옙 占쏙옙 占싱뱄옙占쏙옙占쏙옙 占쏙옙표.
			if(xx>=w||xx<0||yy>=h||yy<0)
			{
				table_right[0][j][i]=-1;
				table_right[1][j][i]=-1;
			}
			else
			{
				table_right[0][j][i]=xx;
				table_right[1][j][i]=yy;
			}
		}
	}

	//printf("adsfsaasdfaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaafd");
	for(j=0;j<h2;j++) //i,j占쏙옙 占쏙옙占� 占싱뱄옙占쏙옙占쏙옙 占쏙옙표
	{
		for(i=i_st;i<i_ed;i++)
		{

			//if(table[0][j][i]==-1||i>739)//占싣뤄옙占쏙옙 占쏙옙占� 占싱곤옙 占쏙옙占쏙옙 left 占쌩쏙옙占쏙옙占쏙옙 占쏙옙.
			if(table[0][j][i]==-1)//rear 占쌩쏙옙.
			{
				if(table_left[0][j][i]==-1)
				{
					if(table_right[0][j][i]!=-1)
					{
						flagtable[j][i]=2;
						table[0][j][i]=table_right[0][j][i];
						table[1][j][i]=table_right[1][j][i];
					}
					else
					{
						flagtable[j][i]=-1;
					}
				}
				else
				{
					flagtable[j][i]=1;
					table[0][j][i]=table_left[0][j][i];
					table[1][j][i]=table_left[1][j][i];
				}
			}
			else
			{
				flagtable[j][i]=0;
			}
		}
	}
	//printf("flag:%d//rear : %d // left : %d\n",flagtable[650][180],table[0][650][180],table_left[0][650][180]);
	//printf("1\n");+350+90+18

	double st,ed;
	//st=clock();
	int x,y;
	for(i = 850;i<1280;i++){
		for(j = 0;j<720;j++){
			for(int kk = 0;kk<3;kk++){
				img2->imageData[3*i+j*img2->widthStep+kk] = 0;
			}
		}
	}
	for(i = 890;i<1280;i++){
		for(j = 0;j<720;j++){
			for(int kk = 0;kk<3;kk++){
				img3->imageData[3*i+j*img3->widthStep+kk] = 0;
			}
		}
	}

	//cvShowImage("3msadf",img2);




//	cv::waitKey(0);

//	printf("i_ed = %d  h2 = %d\n",i_ed,h2+1);
	//printf("test");

	for(i=0;i<1280;i++){
		for(j=0;j<800;j++){
			//printf("gogogogo     %d      \n",(img->imageData+3*table[1][j][i]*w)[3*table[0][j][i]]);

			//if(((img2->imageData+3*table[1][j][i]*w)[3*table[0][j][i]]!=0)){//||((img->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+1]!=0)||((img->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+2]!=0)){
				//if((img2->imageData+3*table[1][j][i]*w)[3*table[0][j][i]]!=0||(img2->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+1]!=0||(img2->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+2]!=0){
				//	flag_Left_Rear[j][i] = 1;

				if(table_left[0][j][i]!=-1 && table_left[0][j][i] > 0 && table_left[0][j][i] <1280 && table_left[1][j][i] > 0 && table_left[1][j][i] <800)
				{
					right_image[j][i] = (uchar)((img2->imageData+3*table_left[1][j][i]*w)[3*table_left[0][j][i]]);
				}

				if(table_right[0][j][i]!=-1 && table_right[0][j][i] > 0 && table_right[0][j][i] <1280 && table_right[1][j][i] > 0 && table_right[1][j][i] <800)
				//if(table_right[0][j][i]!=-1)
				{
					left_image[j][i] = (uchar)((img3->imageData+3*table_right[1][j][i]*w)[3*table_right[0][j][i]]);

				}
				if(flagtable[j][i]==0 && table[0][j][i] > 0 && table[0][j][i] <1280 && table[1][j][i] > 0 && table[1][j][i] <800)
				//if(flagtable[j][i]==0)
				{
					rear_image[j][i] = (uchar)((img->imageData+3*table[1][j][i]*w)[3*table[0][j][i]]);
				}
				//}
				//if((img3->imageData+3*table[1][j][i]*w)[3*table[0][j][i]]!=0||(img3->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+1]!=0||(img3->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+2]!=0){
				//	flag_Right_Rear[j][i] = 1;}
			//}
		}
	}

	for(i=0;i<1280;i++){
		for(j=0;j<800;j++){
			//printf("gogogogo     %d      \n",(img->imageData+3*table[1][j][i]*w)[3*table[0][j][i]]);
			if(table_right[0][j][i]!=-1&&flagtable[j][i]==0){
				Inter_left[j][i] = 128;}
			if(table_left[0][j][i]!=-1&&flagtable[j][i]==0){
					Inter_right[j][i] = 128;}
		}
	}
	//printf("%d\n\n\n\n\n",table_left[0][1][770]);


	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////

	//printf("adsfasdfasdfasdfasdf\n");
//	cv::waitKey(0);

	int minn1 = 1000,minn2 = 1000;
	int x1,x2,y1,y2;
	for(i=0;i<1280;i++){
		for(j=0;j<800;j++){
			if(Inter_left[j][i]>0){
				if((i+j)<minn1){
					minn1 = i+j;

					x1 = i;
					y1 = j;
					//printf("x1 = %d,y1 = %d,minn1 = %d\n",x1,y1,minn1);
				}
				if((i-j)<minn2){
					minn2 = i-j;
					x2 = i;
					y2 = j;}}
		}
	}
	//cv::imshow("pb_image1",Inter_left);

	//printf("x1 = %d,y1 = %d,x2 = %d,y2 = %d\n",x1,y1,x2,y2);

	//printf("\n\n");
	//cv::waitKey(0);
	double aa1;
	aa1 = line2point(2,0,1,0,0,2);
	//printf("sdf %f\n",aa1);

	LOGE("heechul1:%d", 1);

	for(i=0;i<1280;i++){
		for(j=0;j<800;j++){
			if(Inter_left[j][i]>0)
			{
				LOGE("heechul2");
				//LOGE("heechul2:%f", line2point(j,i,y1,x1,y2,x2));

				//if(line2point(j,i,y1,x1,y2,x2)>=0&&line2point(j,i,y1,x1,y2,x2)<33)
				{
					//FMM_left[j][i] = (uchar)(line2point(j,i,y1,x1,y2,x2))+1;
				}
			}
		}
	}
	//cv::imshow("sdfasdf",FMM_left);
    	//cv::waitKey(0);
	/*
	LOGD("test:%d", 1);


	int x3,x4,y3,y4;
	minn1 = 0;minn2 = 0;
	for(i=0;i<1280;i++){
		for(j=0;j<800;j++){
			if(Inter_right[j][i]>0){
				if((i+j)>minn1){
					minn1 = i+j;
					x3 = i;
					y3 = j;
				}
				if((i-j)>minn2){
					minn2 = i-j;
					x4 = i;
					y4 = j;}}
		}
	}

//	double aa1;
	aa1 = line2point(2,0,1,0,0,2);
	//printf("sdf %f\n",aa1);

/// here

	for(i=0;i<1280;i++){
		for(j=0;j<800;j++){
			if(Inter_right.at<uchar>(j,i)>0){
				if(line2point(j,i,y3,x3,y4,x4)>=0&&line2point(j,i,y3,x3,y4,x4)<33){
					FMM_left.at<uchar>(j,i) = (int)(line2point(j,i,y3,x3,y4,x4))+1;}
				else{
					FMM_right.at<uchar>(j,i)=0;}

			}
		}
	}


		cv::Mat sleft_image(50,50,CV_8UC1,cv::Scalar(0));
		cv::Mat srear_image(50,50,CV_8UC1,cv::Scalar(0));


		uchar rear_r,rear_g,rear_b;
		double rear_r1,rear_g1,rear_b1;
		double left_r1,left_g1,left_b1;
		double right_r1,right_g1,right_b1;
		double Ysumleft,Ysumrear;
		double Y;
	double ratt =6;
	double ratt1 =14;
	double rattr,rattg,rattb,ratty;
	double aaa1;
						double aaa_rear;
						double aaa_rgb;

	//printf("333\n");
	for(int count=0;count<24*60*15;count++)
	{
		//printf("count %d\n",count);

		int li1,li2,li3;
		int temp1,temp2,temp3;

		rear_r1 = 0;rear_g1 = 0;rear_b1 = 0;
		left_r1 = 0;left_g1 = 0;left_b1 = 0;
		for(int pj = 259;pj<309;pj++){
			for(int pi = 493;pi<543;pi++){
				rear_r1 = rear_r1+(uchar)((img->imageData+3*table[1][pj][pi]*w)[3*table[0][pj][pi]])*1.0;
				rear_g1=rear_g1+(uchar)((img->imageData+3*table[1][pj][pi]*w)[3*table[0][pj][pi]+1])*1.0;
				rear_b1=rear_b1+(uchar)((img->imageData+3*table[1][pj][pi]*w)[3*table[0][pj][pi]+2])*1.0;
				srear_image.at<uchar>(pj-259,pi-493) = (uchar)((img->imageData+3*table[1][pj][pi]*w)[3*table[0][pj][pi]]);

				left_r1=left_r1+(uchar)((img3->imageData+3*table_right[1][pj][pi]*w)[3*table_right[0][pj][pi]])*1.0;
				left_g1=left_g1+(uchar)((img3->imageData+3*table_right[1][pj][pi]*w)[3*table_right[0][pj][pi]+1])*1.0;
				left_b1=left_b1+(uchar)((img3->imageData+3*table_right[1][pj][pi]*w)[3*table_right[0][pj][pi]+2])*1.0;
				sleft_image.at<uchar>(pj-259,pi-493) = (uchar)((img3->imageData+3*table_right[1][pj][pi]*w)[3*table_right[0][pj][pi]]);
			}
		}
		//cv::imshow("sdfasdf11",srear_image);
		//cv::waitKey(0);
		//cv::imshow("sdfasdf11",sleft_image);
		//cv::waitKey(0);
		rear_r1 = rear_r1/2500;rear_g1 = rear_g1/2500;rear_b1 = rear_b1/2500;
		left_r1 = left_r1/2500;left_g1 = left_g1/2500;left_b1 = left_b1/2500;
		rattr = left_r1/rear_r1;
		rattg = left_g1/rear_g1;
		rattb = left_b1/rear_b1;

		//printf("rattr %f rattg %f rattb %f",rattr,rattg,rattb);


		//printf("rattr %f rattg %f rattb %f\n",rattr,rattg,rattb);


		for(i=0;i<1280;i++) //i,j占쏙옙 占쏙옙占� 占싱뱄옙占쏙옙占쏙옙 占쏙옙표
		{
			for(j=0;j<720;j++)
			{
					//if(flagtable[j][i]==0)// rear
					//{
						//here
						if(img->imageData[3*i+j*img->widthStep]!=0||img->imageData[3*i+j*img->widthStep+1]!=0||img->imageData[3*i+j*img->widthStep+2]!=0){
						rear_r = img->imageData[3*i+j*img->widthStep];
						rear_g=img->imageData[3*i+j*img->widthStep+1];
						rear_b=img->imageData[3*i+j*img->widthStep+2];
						//if(j == 28&&i==760){
						//printf("r = %d g = %d b = %d\n",rear_r,rear_g,rear_b);
						//}
						controlrgb2(rear_r,rear_g,rear_b,rattr,rattg,rattb);
						//if(j == 28&&i==760){
						//printf("aar = %d g = %d b = %d\n",rear_r,rear_g,rear_b);
						//}
						img->imageData[3*i+j*img->widthStep] = (char)(rear_r);
						img->imageData[3*i+j*img->widthStep+1]=(char)(rear_g);
						img->imageData[3*i+j*img->widthStep+2]=(char)(rear_b);
						}
			//}
			}
		}
		//printf("r = %d g = %d b = %d\n",(img->imageData+3*table[1][28][760]*w)[3*table[0][28][760]],(img->imageData+3*table[1][28][760]*w)[3*table[0][28][760]+1],(img->imageData+3*table[1][28][760]*w)[3*table[0][28][760]+2]);
		//img2->imageData[3*i+j*img2->widthStep+kk] = 0;




		rear_r1 = 0;rear_g1 = 0;rear_b1 = 0;
		right_r1 = 0;right_g1 = 0;right_b1 = 0;
		for(int pj = 306;pj<356;pj++){
			for(int pi = 720;pi<770;pi++){
				rear_r1 = rear_r1+(uchar)((img->imageData+3*table[1][pj][pi]*w)[3*table[0][pj][pi]])*1.0;
				rear_g1=rear_g1+(uchar)((img->imageData+3*table[1][pj][pi]*w)[3*table[0][pj][pi]+1])*1.0;
				rear_b1=rear_b1+(uchar)((img->imageData+3*table[1][pj][pi]*w)[3*table[0][pj][pi]+2])*1.0;

				right_r1=right_r1+(uchar)((img2->imageData+3*table_left[1][pj][pi]*w)[3*table_left[0][pj][pi]])*1.0;
				right_g1=right_g1+(uchar)((img2->imageData+3*table_left[1][pj][pi]*w)[3*table_left[0][pj][pi]+1])*1.0;
				right_b1=right_b1+(uchar)((img2->imageData+3*table_left[1][pj][pi]*w)[3*table_left[0][pj][pi]+2])*1.0;
			}
		}
		rear_r1 = rear_r1/2500;rear_g1 = rear_g1/2500;rear_b1 = rear_b1/2500;
		right_r1 = right_r1/2500;right_g1 = right_g1/2500;right_b1 = right_b1/2500;
		rattr = rear_r1/right_r1;
		rattg = rear_g1/right_g1;
		rattb = rear_b1/right_b1;

		//printf("rattr %f rattg %f rattb %f",rattr,rattg,rattb);

		//printf("rattr %f rattg %f rattb %f",rattr,rattg,rattb);
		for(i=0;i<1280;i++) //i,j占쏙옙 占쏙옙占� 占싱뱄옙占쏙옙占쏙옙 占쏙옙표
		{
			for(j=0;j<720;j++)
			{
					//if(flagtable[j][i]==0)// rear
					//{
						//here
						if(img2->imageData[3*i+j*img2->widthStep]!=0||img2->imageData[3*i+j*img2->widthStep+1]!=0||img2->imageData[3*i+j*img2->widthStep+2]!=0){
						rear_r = img2->imageData[3*i+j*img2->widthStep];
						rear_g=img2->imageData[3*i+j*img2->widthStep+1];
						rear_b=img2->imageData[3*i+j*img2->widthStep+2];
						//if(j == 28&&i==760){
						//printf("r = %d g = %d b = %d\n",rear_r,rear_g,rear_b);
						//}
						controlrgb2(rear_r,rear_g,rear_b,rattr,rattg,rattb);
						//if(j == 28&&i==760){
						//printf("aar = %d g = %d b = %d\n",rear_r,rear_g,rear_b);
						//}
						img2->imageData[3*i+j*img2->widthStep] = (char)(rear_r);
						img2->imageData[3*i+j*img2->widthStep+1]=(char)(rear_g);
						img2->imageData[3*i+j*img2->widthStep+2]=(char)(rear_b);
						}
			//}
			}
		}
		printf("%d  %d\n",h2,i_ed);
		for(j=0;j<h2;j++) //i,j占쏙옙 占쏙옙占� 占싱뱄옙占쏙옙占쏙옙 占쏙옙표
		{
			li1=583-(double)(j-187)*169/276;//占쏙옙雍깍옙占�
			li2=729+(double)(j-187)*122/284;//占쏙옙雍깍옙占�
			li3=823-(double)(j-419)*137/23;	//占쏙옙雍깍옙占�
			uchar* ptr1=(uchar*) (undist_img->imageData +j*w2*3);
			for(i=0;i<i_ed;i++)
			{

				if(flagtable[j][i]==-1)
				{
					ptr1[3*i]=0;
					ptr1[3*i+1]=0;
					ptr1[3*i+2]=0;
				}
				else
				{
					if(flagtable[j][i]==0)// rear
					{
						//here
						rear_r = (uchar)((img->imageData+3*table[1][j][i]*w)[3*table[0][j][i]]);
						rear_g=(uchar)((img->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+1]);
						rear_b=(uchar)((img->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+2]);


						//controlrgb2(rear_r,rear_g,rear_b,rattr,rattg,rattb);

						//controlrgb1(rear_r,rear_g,rear_b,ratty);
						//ratt = 25;
						//if(rear_r>(255-ratt)){rear_r = 255;}
						//else{rear_r = floor(rear_r+ratt);}
						//ratt = 5;
						//if(rear_g>(255-ratt)){rear_g = 255;}
						//else{rear_g = floor(rear_g+ratt);}
						//if(rear_b>(255-ratt)){rear_b = 255;}
						//else{rear_b = floor(rear_b+ratt);}


						aaa1 = FMM_right.at<uchar>(j,i);
						if(aaa1>0){

							if(right_image.at<uchar>(j,i)>0){
								//printf("%d",FMM_left.at<uchar>(j,i));
								aaa_rear = rear_r*1.0;
								aaa_rgb = (uchar)((img2->imageData+3*table_left[1][j][i]*w)[3*table_left[0][j][i]])*1.0;
								//if(aaa_rgb<0){aaa_rgb = aaa_rgb+256;}
							ratt = (aaa_rear*aaa1*3.0+aaa_rgb*(100-aaa1*3.0))/100.0;
							//printf("    %f    %f     %f    %f\n",ratt,aaa_rgb,aaa_rear,aaa1);
							rear_r = (int)ratt;

							aaa_rgb = (uchar)((img2->imageData+3*table_left[1][j][i]*w)[3*table_left[0][j][i]+1])*1.0;
								//if(aaa_rgb<0){aaa_rgb = aaa_rgb+256;}
							ratt = (rear_g*aaa1*3.0+aaa_rgb*(100-aaa1*3.0))/100.0;
							rear_g = (int)ratt;

							aaa_rgb = (uchar)((img2->imageData+3*table_left[1][j][i]*w)[3*table_left[0][j][i]+2])*1.0;
								//if(aaa_rgb<0){aaa_rgb = aaa_rgb+256;}
							ratt = (rear_b*aaa1*3.0+aaa_rgb*(100-aaa1*3.0))/100.0;
							rear_b = (int)ratt;
							}
							//printf("\n");
						}

						aaa1 = FMM_left.at<uchar>(j,i);
						if(aaa1>0){

							if(left_image.at<uchar>(j,i)>0){
								//printf("%d",FMM_left.at<uchar>(j,i));
								aaa_rear = rear_r*1.0;
								aaa_rgb =  (uchar)((img3->imageData+3*table_right[1][j][i]*w)[3*table_right[0][j][i]])*1.0;
								//if(aaa_rgb<0){aaa_rgb = aaa_rgb+256;}
							ratt = (aaa_rear*aaa1*3.0+aaa_rgb*(100-aaa1*3.0))/100.0;
							//printf("    %f    %f     %f    %f",ratt,aaa_rgb,aaa_rear,aaa1);
							rear_r = (int)ratt;

							aaa_rgb =  (uchar)((img3->imageData+3*table_right[1][j][i]*w)[3*table_right[0][j][i]+1])*1.0;
								//if(aaa_rgb<0){aaa_rgb = aaa_rgb+256;}
							ratt = (rear_g*aaa1*3.0+aaa_rgb*(100-aaa1*3.0))/100.0;
							rear_g = (int)ratt;

							aaa_rgb =  (uchar)((img3->imageData+3*table_right[1][j][i]*w)[3*table_right[0][j][i]+2])*1.0;
								//if(aaa_rgb<0){aaa_rgb = aaa_rgb+256;}
							ratt = (rear_b*aaa1*3.0+aaa_rgb*(100-aaa1*3.0))/100.0;
							rear_b = (int)ratt;
							//printf("\n");
							}

						}



						ptr1[3*i]=rear_r;
						ptr1[3*i+1]=rear_g;
						ptr1[3*i+2]=rear_b;

					}
					if(flagtable[j][i]==1) //left
					{
						//here
						ptr1[3*i]=(img2->imageData+3*table[1][j][i]*w)[3*table[0][j][i]];
						ptr1[3*i+1]=(img2->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+1];
						ptr1[3*i+2]=(img2->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+2];
					}
					if(flagtable[j][i]==2)//right
					{
						//here
						ptr1[3*i]=(img3->imageData+3*table[1][j][i]*w)[3*table[0][j][i]];
						ptr1[3*i+1]=(img3->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+1];
						ptr1[3*i+2]=(img3->imageData+3*table[1][j][i]*w)[3*table[0][j][i]+2];
					}
				}
			}
		}

	}
	*/
}

#define ALGORITHM_NONE 0
#define ALGORITHM_LDWS 1
#define ALGORITHM_TOPV 2

JNIEXPORT void JNICALL Java_com_teleworks_carmaster_CarmasterVideoFragment_VideoProcessing(JNIEnv*, jobject, jlong addrGray, jlong addrRgba, jlong flags)
{
	int i=0, j=0;
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
/**    vector<KeyPoint> v;


    FastFeatureDetector detector(50);
    detector.detect(mGr, v);

    for( unsigned int i = 0; i < v.size(); i++ )
    {
        const KeyPoint& kp = v[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }
**/

/*****/
    /**
    IplImage *rear, *left, *right, *result;
    rear = cvCreateImage(cvSize(1280, 800), 8, 3);
    left = cvCreateImage(cvSize(1280, 800), 8, 3);
    right = cvCreateImage(cvSize(1280, 800), 8, 3);

    result = cvCreateImage(cvSize(1300, 800), 8, 3);

    LOGE("KAIST start:%d", 1);

    for(i=0; i<rear->nSize; i++)
    {
    	rear->imageData[i] = (char)0;
    	left->imageData[i] = (char)0;
    	right->imageData[i] = (char)0;
    }


    // mRgb占쏙옙占쏙옙占쏙옙 占쌘몌옙占쏙옙 rear, left, right占쏙옙 copy

    Topview(rear, left, right, result);

    LOGE("cols : %d",mRgb.cols);
    LOGE("rows : %d",mRgb.rows);
    **/

/**/
    IplImage *gray, *image;

    gray = cvCreateImage(cvSize(640, 480), 8, 1);
    image = cvCreateImage(cvSize(640, 480), 8, 3);

    cvZero(gray);
	cvZero(image);

    for(i=0; i<480; i++)
    {
    	for(j=10; j<640; j++)
    	{
    		//gray->imageData[i*gray->widthStep+j] = (char)mGr.at<uchar>(j,i);
    		//mRgb.at<Vec3b>(i,j)[0] = 255;
    		image->imageData[i*image->widthStep+j*image->nChannels+2] = (char)mRgb.at<Vec4b>(i,j)[0];
    		image->imageData[i*image->widthStep+j*image->nChannels+1] = (char)mRgb.at<Vec4b>(i,j)[1];
    		image->imageData[i*image->widthStep+j*image->nChannels+0] = (char)mRgb.at<Vec4b>(i,j)[2];
    	}
    }

    cvConvertImage(image, gray, CV_BGR2GRAY);

    if(flags == ALGORITHM_LDWS) {
    	LaneRecognition(image, gray, 4);
    }

    for(i=0; i<480; i++)
    {
    	for(j=0; j<640; j++)
    	{
    		//gray->imageData[i*640] = (char)mGr.at<uchar>(i,j);
    		mRgb.at<Vec4b>(i,j)[0] = (uchar)image->imageData[i*image->widthStep+j*image->nChannels+2];
    		mRgb.at<Vec4b>(i,j)[1] = (uchar)image->imageData[i*image->widthStep+j*image->nChannels+1];
    		mRgb.at<Vec4b>(i,j)[2] = (uchar)image->imageData[i*image->widthStep+j*image->nChannels+0];
    		mRgb.at<Vec4b>(i,j)[3] = (uchar)255;
    	}
    }
    // result 占쏙옙 占싼곤옙占쏙옙占쏙옙占�, 占쏘떤占쏙옙占쏙옙??

    cvReleaseImage(&gray);
    cvReleaseImage(&image);
    /*
    cvReleaseImage(&rear);
    cvReleaseImage(&left);
    cvReleaseImage(&right);
    cvReleaseImage(&result);
*/
}
}
