#include "LaneDetect.h"

#define MAX_LANE_NUM 4

using namespace std;

// For debugging
IplImage *draw = NULL;
char filename[256];

float score[MAX_LANE_NUM] = {0,};
float likelihood[MAX_LANE_NUM] = {0,};

unsigned int candidate_num = 0;
unsigned int lane_width = 40;
int state[100];
int number_of_lane = 0;

void set_number_of_lane(int l)
{
	number_of_lane = l;
}

float get_likelihood(int lane)
{
	return likelihood[lane-1];
}
int get_number_of_lane()
{
	return number_of_lane;
}

int max_likelihood()
{
	float max_val = 0;
	int max_idx = 0;
	int i=0;

	for(i=0; i<MAX_LANE_NUM; i++)
	{
		if(max_val < likelihood[i])
		{
			max_val = likelihood[i];
			max_idx = i+1;
		}
	}

	return max_idx;
}

int max_score()
{
	float sum_val = 0;
	int i=0;

	for(i=0; i<MAX_LANE_NUM; i++)
	{
		sum_val += score[i];
	}

	for(i=0; i<MAX_LANE_NUM; i++)
	{
		likelihood[i] = score[i] / sum_val;
	}

	//printf("Likelihood : %f %f %f %f\n", likelihood[0], likelihood[1], likelihood[2], likelihood[3]);

	return max_likelihood();
}

void shift_left()
{
	int i=0;

	for(i=0; i<MAX_LANE_NUM-1; i++)
	{
		score[i] = score[i+1];
	}
	score[MAX_LANE_NUM-1] = 0;
}

void shift_right()
{
	int i=0;

	for(i=MAX_LANE_NUM; i>0; i--)
	{
		score[i] = score[i-1];
	}
	score[0] = 0;
}

void initialize_score()
{
	int i=0;

	for(i=0; i<MAX_LANE_NUM; i++)
	{
		score[i] = 0;
		likelihood[i] = 0;
	}
}

void decay_score()
{
	int i=0;

	for(i=0; i<MAX_LANE_NUM; i++)
	{
		score[i] = 0.9 * score[i]; // 0.99
	}
}

void add_score(int idx, float value)
{
	score[idx] = score[idx] + value;
}

float cal_dist(float a, float b)
{
	return abs(a-b);
}

int min_lane(int *line_pos, int *cand_num)
{
	int i=0;
	float min_val = 10000;
	int min_idx = 0;

	for(i=0; i<*cand_num; i++)
	{
		if(min_val > cal_dist(line_pos[i],350))
		{
			min_val = cal_dist(line_pos[i],350);
			min_idx = i;
		}
	}

	for(i=0; i<*cand_num; i++)
	{
		if(min_val > cal_dist(line_pos[i],320))
		{
			min_val = cal_dist(line_pos[i],320);
			min_idx = i;
		}
	}

	return min_idx;
}

int nearest_lane(int *line_pos, int cand_num, int x)
{
	int i=0;
	float min_val = 10000;
	int min_idx = 0;

	for(i=0; i<cand_num; i++)
	{
		if(min_val > cal_dist(line_pos[i],x))
		{
			min_val = cal_dist(line_pos[i],x);
			min_idx = i;
		}
	}

	return min_idx;
}

int is_right_lane(int *line_pos, int *idx, int number_of_lane)
{
	int i=0, j=0;

	for(i=0; i<number_of_lane+1; i++)
	{
		for(j=0; j<number_of_lane+1; j++)
		{
			if(i != j)
			{
				if(line_pos[idx[i]] == line_pos[idx[j]])
					return 0;
			}
		}
	}

//	printf("\n");

	return 1;
}

float cal_dist(int x, int y)
{
	return (x-y)*(x-y);
}

float cal_histgram_error(float hist[][256], int num)
{
	int i=0, j=0, k=0;
	float sum=0, result=0;
	int cnt=0;

	for(i=0; i<num; i++)
	{
		for(j=0; j<num; j++)
		{
			sum = 0;

			if(i != j)
			{
				for(k=0; k<256; k++)
				{
					sum += cal_dist(hist[i][k], hist[j][k]);
				}
				cnt++;
			}

			result += sqrt(sum);
		}
	}

	return result/cnt;
}

void filtering(int *line_pos, int cand_num, int *state, int min_idx, int number_of_lane, IplImage *img)
{
	int i=0, j=0, k=0, x=0, xl=0, xr=0;
	int start=0, end=0, success=0;
	float sum_err = 0, min_err = 10000;
	int result_idx[100] = {0,}, final_idx[100] = {0,};
	CvRect rect;

	int bins = 256;
	int sizes[] = {bins};
	float xranges[] = {0, 255};
	float* ranges[] = {xranges};
	float min_value = 0, max_value = 0;
	float hists[100][256] = {0,};

	CvHistogram *hist = cvCreateHist(1, sizes, CV_HIST_ARRAY, ranges, 1);

	for(j=0; j<number_of_lane+1; j++)
	{
		start = number_of_lane-j;
		end = j;

		//printf("number of lane : %d\n", number_of_lane);

		sum_err = 0;
		success = 0;

		for(i=-start; i<=end; i++)
		{
			x = line_pos[min_idx] + i * lane_width;

			//printf("%d ", i);

			result_idx[i+start] = nearest_lane(line_pos, cand_num, x);

			if(x > line_pos[min_idx] && line_pos[min_idx] < 335 && x > 335)
				success = 1;
			else if(x < line_pos[min_idx] && line_pos[min_idx] > 335 && x < 335)
				success = 1;

			sum_err += cal_dist(line_pos[result_idx[i+start]], x);
		}

		//printf("\n");


		sum_err /= number_of_lane;


		if(is_right_lane(line_pos, result_idx, number_of_lane))
		{

			for(i=0; i<number_of_lane; i++)
			{
				rect.x = line_pos[result_idx[i]];
				rect.width = line_pos[result_idx[i+1]] - line_pos[result_idx[i]];
				rect.y = 0;
				rect.height = 400;

				cvSetImageROI(img, rect);
				cvCalcHist(&img, hist);
				cvResetImageROI(img);
				cvSetReal1D(hist->bins, 0, 0);
				cvGetMinMaxHistValue(hist, &min_value, &max_value);

				for(k=0; k<255; k++)
				{
					hists[i][k] = cvGetReal1D(hist->bins, k) / max_value;
				}
			}

			sum_err = cal_histgram_error(hists, number_of_lane);

			//printf("error : %f\t%f\n", sum_err, cal_histgram_error(hists, number_of_lane));


			if(sum_err < min_err && success == 1)
			{
				min_err = sum_err;

				for(i=0; i<number_of_lane+1; i++)
				{
					final_idx[i] = result_idx[i];

				}
			}
		}
	}

	for(i=0; i<cand_num; i++)
	{
		state[i] = 0;
	}

	//if(is_right_lane(line_pos, final_idx, number_of_lane))
	if(min_err < 60)
	{
		for(i=0; i<number_of_lane+1; i++)
		{
			state[final_idx[i]] = 1;
		}
	}

	cvReleaseHist(&hist);
}

void Erase(IplImage *src, IplImage *dst)
{
	int i=0;

	for(i=2; i<src->imageSize-2; i++)
	{
		if((unsigned char)src->imageData[i] == 0 && (unsigned char)src->imageData[i+1] != 0)
		{
			dst->imageData[i-3] = 0;
			dst->imageData[i-2] = 0;
			dst->imageData[i-1] = 0;
			dst->imageData[i] = 0;
			dst->imageData[i+1] = 0;
			dst->imageData[i+2] = 0;
			dst->imageData[i+3] = 0;
		}
		else if((unsigned char)src->imageData[i] != 0 && (unsigned char)src->imageData[i+1] == 0)
		{
			dst->imageData[i-3] = 0;
			dst->imageData[i-2] = 0;
			dst->imageData[i-1] = 0;
			dst->imageData[i] = 0;
			dst->imageData[i+1] = 0;
			dst->imageData[i+2] = 0;
			dst->imageData[i+3] = 0;
		}
	}
}

double CLaneDetect::DeltaRad (double ang1, double ang2)
{
	double da = ang1 - ang2;
	if (-M_PI < da && da < M_PI) return da;
	else {
		da = fmod (da, 2*M_PI);
		if (M_PI <= da) return da - 2*M_PI;
		else if (da <= -M_PI) return da + 2*M_PI;
		else return da;
	}
	return da;
}

int CLaneDetect::LaneDetect (IplImage *src, IplImage *ipm, IplImage *dest,CvMat *mat, int mode)
{
	number_of_lane = get_number_of_lane();

	// For debugging
	draw = dest;
	int lane_num=0;

	CvMat *mat2;
	CvMat *mul;
	mul = cvCreateMat(3,3,CV_32FC1);
	mat2 = cvCreateMat(3,3,CV_32FC1);
	cvmInvert(mat, mat2);

	// homography 역행렬 구하기 (나중에 원래 영상으로 차선을 warping 하는데 사용됨)
	float a1=cvmGet(mat,0,0);
	float a2=cvmGet(mat,0,1);
	float a3=cvmGet(mat,0,2);
	float a4=cvmGet(mat,1,0);
	float a5=cvmGet(mat,1,1);
	float a6=cvmGet(mat,1,2);
	float a7=cvmGet(mat,2,0);
	float a8=cvmGet(mat,2,1);
	float a9=cvmGet(mat,2,2);


	if(mode)
		initialize_score();

	//cvmMul(mat2,mat,mul);
	// 0 ~ 255 그래이 스케일로 변환한다.
	IplImage* gray = cvCreateImage( cvGetSize(src), IPL_DEPTH_8U, 1);

	// 0. ~ 1. 그래이 스케일로 변환한다.
	IplImage *img_32f = cvCreateImage(cvGetSize(ipm), IPL_DEPTH_32F, 1);


	cvCvtColor(ipm, gray, CV_RGB2GRAY);
	//yellow_image(ipm, gray);


	cvConvertScale(gray, img_32f, 1.0 / 255.0, 0);

	//cvSmooth(img_32f, img_32f, CV_GAUSSIAN,0,3,3);

	// Sobel 연산자로 이미지를 x, y 방향으로 미분한다. 즉, edge 검출.
	IplImage *diff_x = cvCreateImage(cvGetSize(img_32f), IPL_DEPTH_32F, 1);
	IplImage *diff_y = cvCreateImage(cvGetSize(img_32f), IPL_DEPTH_32F, 1);
	cvSobel(img_32f, diff_x, 1, 0, 3); // x방향 edge 성분
	//cvSobel(img_32f, diff_y, 0, 1, 3); // y방향 edge 성분
	cvZero(diff_y);

	// Edge의 magnitude와 orientation을 계산한다.
	IplImage *mag = cvCreateImage(cvGetSize(img_32f), IPL_DEPTH_32F, 1);
	IplImage *ori = cvCreateImage(cvGetSize(img_32f), IPL_DEPTH_32F, 1);
	cvCartToPolar(diff_x, diff_y, mag, ori, 0); // magnitude 와 orientation 계산

	// Lane 후보 점들을 기록할 이미지를 만든다.
	IplImage *mag2 = cvCreateImage (cvGetSize(mag), IPL_DEPTH_8U, 1);
	cvZero (mag2);


	// Lane의 후보가 될 점들을 찾는다.
	//cvThreshold(mag, mag2, 0.21, 255, CV_THRESH_BINARY);
	cvThreshold(mag, mag2, 0.3, 255, CV_THRESH_BINARY);

	Erase(gray, mag2);

	//LaneCandidate (mag, ori, mag2);


	char fps_str2[20];
	CvFont font2;
	sprintf(fps_str2, "C A R");
	cvInitFont(&font2, CV_FONT_HERSHEY_SIMPLEX,0.5,0.2,0,1,8);

	//cvPutText(ipm,fps_str2, cvPoint(320, 390),&font2, CV_RGB(255,0,0));

	cvRectangle(ipm,cvPoint(335 - lane_width/2+5,400),cvPoint(335 + lane_width/2-5,480),CV_RGB(255,0,0),3);


	//cvLine(ipm,cvPoint(174,0),cvPoint(222,480),cvScalar(255,0,0),2);
	//cvLine(ipm,cvPoint(391,0),cvPoint(391,480),cvScalar(255,0,0),2);

	//cvNamedWindow("src",1);
	//cvShowImage("src",ipm);
	//cvNamedWindow("ipm",1);
	//cvShowImage("ipm",mag2);

	//cvNamedWindow("magnit",1);
	//cvShowImage("magnit",src);

	// Lane 후보 점들을 저장할 공간 확보
	//////vector<sLine> cand;
	sLine cand[1000];
	/////cand.reserve(1000);
	//////vector<sLine> cand2;
	sLine cand2[1000];
	//////cand2.reserve(1000);

	CvMemStorage* storage = cvCreateMemStorage(0);
	IplImage *fit = cvCreateImage(cvGetSize(src),8,1);
	cvZero(fit);
	// cvHoughlLines2
	// threshold - A line is returned by the function if the corresponding accumulator value is greater than threshold
	// param1 - minimum line length
	// param2 - maximum gap between line segments lying on the same line to treat them as a single line segment
	int x1;
	int x2;
	int y1;
	int y2;
	int i=0;

	int c1;
	int c2;
	CvPoint2D32f srcPts[2] = {0}, dstPts[2] = {0};
	double x3;
	double y3;
	double x4;
	double y4;

	CvMat _pt1 = cvMat(1, 2, CV_32FC2, &srcPts[0] );
	CvMat _pt2 = cvMat(1, 2, CV_32FC2, &dstPts[0] );

	//double thres = 20.;
	double thres = 20.;
	int left=0;
	int right=0;
	int left_x;
	int num_line=0;
	int line_pos[100];
	int cand_num=0;
	int thr=20;
	int idx=0;


	//CvSeq *lines = cvHoughLines2 (mag2, storage, CV_HOUGH_PROBABILISTIC, 1, CV_PI/180, 5, 2, 10);
	CvSeq *lines = cvHoughLines2 (mag2, storage, CV_HOUGH_PROBABILISTIC, 1, CV_PI/180, 20, 2, 10);
	// 차선 후보군으로부터 hough transform을 이용하여 차선 segment 성분 추출
	for (i = 0; i < lines->total; i++)
	{
		CvPoint* line = (CvPoint*)cvGetSeqElem(lines,i);

		//////cand.push_back (sLine(line[0].x, line[0].y, line[1].x, line[1].y));
		cand[idx].sx = line[0].x;
		cand[idx].sy = line[0].y;
		cand[idx].dx = line[1].x;
		cand[idx].dy = line[1].y;
		idx++;

		double len2 = sqrt((double)(pow((line[1].x-line[0].x),2.)+pow((line[1].y-line[0].y),2.)));

		if(len2>thres) // segment 가 일정 길이 이상이면
		{
			int chk=0; // top-view에서 차선인식
			if(cand_num==0)
			{
				line_pos[cand_num]=line[0].x;
				//cvLine (ipm, cvPoint(line_pos[cand_num], 0), cvPoint(line_pos[cand_num],480),CV_RGB(0,255,0), 2, 8);
				cand_num++;
			}
			else
			{
				for (int ca=0; ca<cand_num; ca++)
				{
					if(line[0].x < line_pos[ca]+thr && line[0].x > line_pos[ca]-thr)
					{
						chk++;
						break;
					}
				}
				if(chk==0)
				{
					line_pos[cand_num]=line[0].x;
					//cvLine (ipm, cvPoint(line_pos[cand_num], 0), cvPoint(line_pos[cand_num],480),CV_RGB(0,255,0),2, 8);
					cand_num++;

					// 역변환을 통해 원래 영상에 차선을 warping 하는 부분

					//x1 = line[0].x;
					//y1 = line[0].y;
					//x2 = line[1].x;
					//y2 = line[1].y;

					//srcPts[0]=cvPoint2D32f(x1,y1);
					//srcPts[1]=cvPoint2D32f(x2,y2);

					//cvPerspectiveTransform( &_pt1, &_pt2, mat );
					//x3=dstPts[0].x;
					//y3=dstPts[0].y;
					//x4=dstPts[1].x;
					//y4=dstPts[1].y;

					//cand2.push_back(sLine(x3,y3,x4,y4));


					//if (x1>0 && y1>0 && x2>0 && y2>0)
					//	cvLine(src,cvPoint(x3,y3),cvPoint(x4,y4),CV_RGB(0,255,0), 3, 8);

				}
			}

			//cvLine (ipm, cvPoint(line_pos[cand_num], 0), cvPoint(line_pos[cand_num],480),CV_RGB(0,0,0), 2, 8);

		}

		// For Debugging
		//cvLine (fit, line[0], line[1], CV_RGB(255,255,255), 2, 8);
	}


	//printf("candidate num : %d\n", cand_num);

	int min_idx = min_lane(line_pos, &cand_num);


	filtering(line_pos, cand_num, state, min_idx, number_of_lane, gray);

	for(i=0; i<cand_num; i++)
	{
//		if(min_idx == i)
			//cvLine (ipm, cvPoint(line_pos[i], 0), cvPoint(line_pos[i],480),CV_RGB(255,0,0), 2, 8);
//		else
			//cvLine (ipm, cvPoint(line_pos[i], 0), cvPoint(line_pos[i],480),CV_RGB(0,255,0), 2, 8);
//		else
		if(state[i] == 1)
		{
			cvLine (ipm, cvPoint(line_pos[i], 0), cvPoint(line_pos[i],480),CV_RGB(0,0,255), 2, 8);

		}
		else
		{
			//cvLine (ipm, cvPoint(line_pos[i], 0), cvPoint(line_pos[i],480),CV_RGB(0,255,0), 2, 8);
		}

	}

	//for (int i=0; i<cand2.size(); i++)
	//{
	//	double slope =(double) (cand2[i].ey-cand2[i].sy)/(cand2[i].ex-cand2[i].sx);
	//	int y_cross =(cand2[i].ey-slope*cand2[i].ex);
	//	int x_cross=(300-y_cross)/slope;
	//	int x_cross2 = (400-y_cross)/slope;
	//	//cvLine (src, cvPoint(x_cross,300 ), cvPoint(x_cross2,400), CV_RGB(0,255,0), 2, 8);

	//}

	 // 원래 영상에서 소실점을 구해 차선을 인식하는 부분 (생략)
	//vector<sLine> inlier;
	//sPoint vp = VanishingPoint (cand2, &inlier);

	// 소실점 표시
	//cvCircle (src, cvPoint(vp.x, vp.y), 5, CV_RGB(0,255,0), 3, 8);

	// Lane의 후보 점들 표시
	//int thres_gap=50;
	//for (unsigned int i=0; i<inlier.size(); i++) {

		//double ymax= max(inlier[i].sy,inlier[i].ey);
		//if(ymax > vp.y && (mag->width/2-thres_gap<vp.x && vp.x<mag->width/2+thres_gap)){
			//	cvLine (draw, cvPoint(inlier[i].sx, inlier[i].sy), cvPoint(inlier[i].ex, inlier[i].ey), CV_RGB(0,255,0), 2, 8);
	//		double slope =(double) (inlier[i].ey-inlier[i].sy)/(inlier[i].ex-inlier[i].sx);
	//		int y_cross =(inlier[i].ey-slope*inlier[i].ex);
	//		int x_cross=(300-y_cross)/slope;
	//		int x_cross2 = (400-y_cross)/slope;
	//		cvLine (src, cvPoint(x_cross,300 ), cvPoint(x_cross2,400), CV_RGB(0,255,0), 2, 8);
		//}
	//}


	int th=20; // 좌우 이동유무 측정
	int move=0;

	for(i=0; i<cand_num; i++)
	{
		if(state[i] && line_pos[i]<320+th)
		{
			left++;
		}
		else if(state[i] && line_pos[i]>350-th)
		{
			right++;
		}
		else
		{
			move++;
		}
	}

	if(number_of_lane < cand_num && left != 0)
	{
		decay_score();
		score[left-1] =  score[left-1] + 1;
		lane_num = left;
	}
	else
	{
		lane_num = 0;
	}

//	printf("Score : %f %f %f %f\n", score[0], score[1], score[2], score[3]);


	//sprintf(filename,"D:/projectpd/result/teleworks3/%d.jpg",cnt);
	//cvSaveImage(filename,ipm);
	//cvWarpPerspective(ipm, fit, mat2, CV_INTER_LINEAR | CV_WARP_INVERSE_MAP | CV_WARP_FILL_OUTLIERS, cvScalarAll(0) );
	//printf("왼쪽: %d, 오른쪽: %d, 이동: %d\n",left,right,move);
	cvReleaseMemStorage (&storage);

	char fps_str[20];
	CvFont font;
	sprintf(fps_str, "Lane: %d", lane_num);
	cvInitFont(&font, CV_FONT_HERSHEY_SIMPLEX,1.,1.,0,2,8);
	//cvPutText(ipm,fps_str, cvPoint(5, 35),&font, CV_RGB(0,255,0));

	//cvNamedWindow("src",1);
	//cvShowImage("src",src);

//	cvNamedWindow("Top_View",1);
//	cvShowImage("Top_View",mag2);

	candidate_num++;


	// Lane의 후보 점들로부터 소실점(vanishing point)를 계산해서,
	// 이 소실점에 대한 inlier 점들을 걸러낸다.

//	cvShowImage ("result", dest);

//	cvReleaseImage(&ipm);
	cvReleaseImage(&fit);
	cvReleaseImage(&img_32f);
	cvReleaseImage(&diff_x);
	cvReleaseImage(&diff_y);
	cvReleaseImage(&mag2);
	cvReleaseImage(&mag);
	cvReleaseImage(&ori);

	cvReleaseImage(&gray);

	cvReleaseMat(&mul);
	cvReleaseMat(&mat2);

	return lane_num;
}

struct sLaneCand {
	int x, y;
	float mag;
	float ori;

	sLaneCand() : x(0), y(0), mag(0.), ori(0.) { }
	sLaneCand(int x_, int y_, float m_, float o_)
		: x(x_), y(y_), mag(m_), ori(o_) { }
};

void CLaneDetect::LaneCandidate (IplImage *mag, IplImage *ori, IplImage *dst)
{
	// 이미지 상의 한 픽셀의 위치를 y,x로 지정하여 픽셀의 참조 포인트 값을 계산한다.
	#define pPixel(img,y,x)	(img->imageData + (y)*img->widthStep + img->nChannels*img->depth/8*(x))

	CvPoint pts[3] = { {0, mag->height}, {mag->width/2, 0}, {mag->width, mag->height} };

	// 한 스캔 라인에서 Lane 후보 점들을 찾기위해 임시로 필요한 공간 확보
	//cv::vector<sLaneCand> tmp;
	//tmp.reserve(mag->width);
	sLaneCand tmp2[640];
	int idx = 0;

	for (int h=0; h<mag->height; h++) {
		//tmp.clear();
		idx = 0;

		float *m = (float *)pPixel(mag,  h, 0);
		float *o = (float *)pPixel(ori,  h, 0);
		char  *d = (char  *)pPixel(dst,  h, 0);

		for (int w=1, nw=mag->width-1; w<nw; w++) {

			    // orientation이 수직에 가까운 점들은 Lane 후보가 될 수 없다.
				const double margin = 0.05;
			    // orientation이 수직에 가까운 성분들 제거
				if ((M_PI*2-margin <o[w] || o[w] < +margin) ||
					(M_PI-margin < o[w] && o[w] < M_PI+margin) )
				{
			// magnitude가 threshold를 넘어서면서 최대가 되는 점을 찾는다.
			const double threshold = 0.1;

			if (threshold < m[w] && m[w-1] < m[w] && m[w] > m[w+1]) {
				// For Debugging: 흐리게 이미지 상에 표시한다.
				// d[w] = (m[w] <= 1.) ? m[w]*255 : 255;

				// 이 점을 일단 tmp에 저장해 뒀다가 다시 Lane 후보가 되기 위한 다른 조건들을 추가로 살펴본다.
				//tmp.push_back (sLaneCand(w, h, m[w], o[w]));
				tmp2[idx].x = w;
				tmp2[idx].y = h;
				tmp2[idx].mag = m[w];
				tmp2[idx].ori = o[w];

				idx++;
			}
				}
		}

		// 차선의 폭을 결정
		double lane_width = 40;
		int lane_width_lo = -10 + (int)(lane_width*0.8);
		int lane_width_hi = 10 + (int)(lane_width*1.2);

		// 차선 후보군 조건 1: 서로 밝기(magnitude)가 비슷해야 한다.
		//			조건 2: 서로 방향(orientation)이 180도 차이가 나야한다.
		const float mag_threshold = 1.2f;
		const float ori_threshold = 0.8f;

		//for (int i=0; i<(int)tmp.size()-1; i++) {
		for (int i=0; i<idx; i++) {

			//if (M_PI/2 < tmp[i].ori && tmp[i].ori < M_PI*3/2){
			//	continue;
			//}


			//for (int j=i+1; j<(int)tmp.size(); j++) {
			for (int j=i+1; j<idx; j++) {
				//int width = tmp[j].x - tmp[i].x;
				int width = tmp2[j].x - tmp2[i].x;

				if (0 <= width && width <= 10) {
//					float mag_err = (tmp[j].mag - tmp[i].mag)/(tmp[j].mag + tmp[i].mag);
//					float ori_err = DeltaRad (DeltaRad (tmp[j].ori, tmp[i].ori), M_PI);
					float mag_err = (tmp2[j].mag - tmp2[i].mag)/(tmp2[j].mag + tmp2[i].mag);
					float ori_err = DeltaRad (DeltaRad (tmp2[j].ori, tmp2[i].ori), M_PI);

					if ((-mag_threshold < mag_err && mag_err < +mag_threshold) &&
						(-ori_threshold < ori_err && ori_err < +ori_threshold)) {
//							int x_mid = (tmp[j].x + tmp[i].x)/2;
//							float mag_avg = (tmp[j].mag + tmp[i].mag)/2.f;
							// 평균을 낼 때 edge에 수직인 각을 pi/2를 더해 edge에 평행하게 바꾼다.
//							float ori_avg = (tmp[j].ori + tmp[i].ori)/2.f + M_PI;

							int x_mid = (tmp2[j].x + tmp2[i].x)/2;
							float mag_avg = (tmp2[j].mag + tmp2[i].mag)/2.f;
							// 평균을 낼 때 edge에 수직인 각을 pi/2를 더해 edge에 평행하게 바꾼다.
							/////float ori_avg = (tmp2[j].ori + tmp2[i].ori)/2.f + M_PI;

							d[x_mid] = (mag_avg <= 0.01) ? (int)(mag_avg*255) : 255;

							//cvCircle(dst, cvPoint(x_mid, 100), 2, CV_RGB(0,0,255));
					}
				}

				//else if (width > lane_width_hi) {
				//	break;
				//}
			}
		}
	}
}



CLaneDetect _lane;

char filename2[256];
int max_lane=0;
CvMat *mat;

int Max(const int *Numbers, const int Count)
{

	int Maximum = Numbers[0];
	int idx=0;

	for(int i = 0; i < Count; i++)
		if( Maximum <= Numbers[i] )
		{
				Maximum = Numbers[i];
				idx=i;
		}
	return idx;
}

static CvPoint ipm_table[640][480];

int DoLaneDetect(IplImage *src, int mode, int start)
{
	int sel_lane = 0;
	//IplImage* src =cvLoadImage("D:/deformable/rectified.jpg");
	IplImage *gray=0;
	IplImage *gray2=0;
	CvRect roi;
	int i=0, j=0;
	float coord[3] = {0,}, result_coord[3] = {0,};

	if (src) {
		// 이미지에서 실제로 lane을 찾을 영역을 설정한다.
		roi = cvRect (0, 0, 640, 480);
		cvSetImageROI (src, roi);

		IplImage* src_roi = cvCreateImage( cvSize(roi.width, roi.height), src->depth, src->nChannels);

		cvCopy (src, src_roi);

		cvResetImageROI (src);
		//------------------------------------------------

		IplImage* dst = cvCreateImage( cvGetSize(src_roi), IPL_DEPTH_8U, 3);

		cvCopy (src_roi,dst);

		CvPoint2D32f ptSource[4], ptPrespective[4];

		/************ 루카스*/

		ptSource[0] = cvPoint2D32f(230, 347); // 직사각형 좌표 입력 (입력 영상)
		ptSource[1] = cvPoint2D32f(344, 348);
		ptSource[2] = cvPoint2D32f(190, 399);
		ptSource[3] = cvPoint2D32f(380, 400);

		ptPrespective[0]  = cvPoint2D32f(325,400); // 변환된 사각형 좌표 입력
		ptPrespective[1]  = cvPoint2D32f(345,400);//(dst->width, 0);
		ptPrespective[2] = cvPoint2D32f(325,480);//(dst->width, dst->height);
		ptPrespective[3] = cvPoint2D32f(345,480);//(0, dst->height);

		/******************/
/*
		ptSource[0] = cvPoint2D32f(222, 347); // 직사각형 좌표 입력 (입력 영상)
		ptSource[1] = cvPoint2D32f(344, 348);
		ptSource[2] = cvPoint2D32f(174, 399);
		ptSource[3] = cvPoint2D32f(391, 400);

		ptPrespective[0]  = cvPoint2D32f(315,400); // 변환된 사각형 좌표 입력
		ptPrespective[1]  = cvPoint2D32f(345,400);//(dst->width, 0);
		ptPrespective[2] = cvPoint2D32f(320,480);//(dst->width, dst->height);
		ptPrespective[3] = cvPoint2D32f(350,480);//(0, dst->height);


		//ptSource[0] = cvPoint2D32f(277, 309); // 직사각형 좌표 입력 (입력 영상)
		//ptSource[1] = cvPoint2D32f(374, 306);
		//ptSource[2] = cvPoint2D32f(206, 369);
		//ptSource[3] = cvPoint2D32f(444, 360);

*/
		/******** 파인뷰 *********
		ptSource[0] = cvPoint2D32f(280, 309); // 직사각형 좌표 입력 (입력 영상)
		ptSource[1] = cvPoint2D32f(374, 306);
		ptSource[2] = cvPoint2D32f(206, 369);
		ptSource[3] = cvPoint2D32f(444, 360);


		ptPrespective[0]  = cvPoint2D32f(310,400); // 변환된 사각형 좌표 입력
		ptPrespective[1]  = cvPoint2D32f(360,400);//(dst->width, 0);
		ptPrespective[2] = cvPoint2D32f(310,480);//(dst->width, dst->height);
		ptPrespective[3] = cvPoint2D32f(360,480);//(0, dst->height);

		*******************/

		float newm[9];
		float newm2[9];

		mat = cvCreateMat(3,3,CV_32FC1); // 두 영상간의 homography 초기화
		//inv = cvCreateMat(3,3,CV_32FC1); // 두 영상간의 homography 초기화

		//cvInvert(mat, inv);

		gray2 = cvCreateImage(cvSize(src->width,src->height),8,3); // 변환된 탑뷰 이미지
		cvZero(gray2);

		if(start == 1)
		{
			cvGetPerspectiveTransform(ptPrespective, ptSource, mat);  // homography 구하기

			cvmSet(mat, 2, 2, 1); // 3행 3열 1로 설정

			//cvWarpPerspective(src_roi, gray2, mat, CV_INTER_LINEAR | CV_WARP_INVERSE_MAP | CV_WARP_FILL_OUTLIERS, cvScalarAll(0));

			for(i=0; i<gray2->height; i++)
			{
				for(j=0; j<gray2->width; j++)
				{
					coord[0] = j;
					coord[1] = i;
					coord[2] = 1;

					result_coord[0] = coord[0] * mat->data.fl[0*3+0] + coord[1] * mat->data.fl[0*3+1] + coord[2] * mat->data.fl[0*3+2];
					result_coord[1] = coord[0] * mat->data.fl[1*3+0] + coord[1] * mat->data.fl[1*3+1] + coord[2] * mat->data.fl[1*3+2];
					result_coord[2] = coord[0] * mat->data.fl[2*3+0] + coord[1] * mat->data.fl[2*3+1] + coord[2] * mat->data.fl[2*3+2];

					if(result_coord[2] != 0)
					{
						ipm_table[j][i].x = (int)(result_coord[0] / result_coord[2]);
						ipm_table[j][i].y = (int)(result_coord[1] / result_coord[2]);

						if(ipm_table[j][i].x >= src->width)
							ipm_table[j][i].x = 0;
						else if(ipm_table[j][i].x < 0)
							ipm_table[j][i].x = 0;

						if(ipm_table[j][i].y >= src->height)
							ipm_table[j][i].y = 0;
						else if(ipm_table[j][i].y < 0)
							ipm_table[j][i].y = 0;

					}
					else
					{
						ipm_table[j][i].x = 0;
						ipm_table[j][i].y = 0;
					}
				}
			}

		}
		else
		{
			for(i=0; i<gray2->height; i++)
			{
				for(j=0; j<gray2->width; j++)
				{
					if(ipm_table[j][i].y != 0 && ipm_table[j][i].x != 0)
					{
						gray2->imageData[i*gray2->widthStep+j*gray2->nChannels+0] = src->imageData[ipm_table[j][i].y*src->widthStep+ipm_table[j][i].x*src->nChannels+0];
						gray2->imageData[i*gray2->widthStep+j*gray2->nChannels+1] = src->imageData[ipm_table[j][i].y*src->widthStep+ipm_table[j][i].x*src->nChannels+1];
						gray2->imageData[i*gray2->widthStep+j*gray2->nChannels+2] = src->imageData[ipm_table[j][i].y*src->widthStep+ipm_table[j][i].x*src->nChannels+2];
					}
				}
			}
		}
		sel_lane =_lane.LaneDetect(src,gray2,dst,mat,mode); // 차선 인식 및 차로 결정 함수 호출

		cvCopy(gray2, src);

		cvReleaseImage(&dst);
		cvReleaseImage(&src_roi);
		cvReleaseMat(&mat);
	}

	//cvReleaseImage(&gray);
	cvReleaseImage(&gray2);
	//cvReleaseImage(&src);

	return sel_lane;
}
