#include "lane.h"
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

//static int left_coord[4][100] = {0,}, right_coord[4][100] = {0,};
static PointKAIST prev_pt_left[3] = {0,}, prev_pt_right[3] = {0,};
static PointKAIST prev_prev_pt_left[3] = {0,}, prev_prev_pt_right[3] = {0,};
static int center = 0;
static int mask_size = 5;
static int mask_size_2 = 25;
static int margin[FILTER_NUM];
static int interval[FILTER_NUM];//interval = step*(height/2-170)/max_step; // 100;
static int sum_buf[FILTER_NUM][1280] = {0,};
static int start = 1;
static int prev_warning = 0;
static int horizontal_center = 0;
static int warning_val = 0;
static int width = 0;
static int vertical_center = 0;

static int interval_cal[24] = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 210, 220, 230};
static int sum_buf_cal[24][640] = {0,};

static int left_margin = 0, right_margin = 0, bottom_margin = 0, vanishing_th = 0;

static PointKAIST prev_result_l[2], prev_result_r[2];

static double mean_x[3]={0,}, mean_y[3]={0,};
static double mean_x2[3]={0,}, mean_y2[3]={0,};

static float *mean_img = 0, *square_mean_img = 0, *var_img = 0;

void init(PointKAIST *pt_left, PointKAIST *pt_right)
{
	center = 320;

	pt_left[0].x = 0;
	pt_left[0].y = 0;
	pt_left[1].x = 0;
	pt_left[1].y = 0;
	pt_left[2].x = 0;
	pt_left[2].y = 0;


	pt_right[0].x = 0;
	pt_right[0].y = 0;
	pt_right[1].x = 0;
	pt_right[1].y = 0;
	pt_right[2].x = 0;
	pt_right[2].y = 0;

	prev_pt_left[0].x = 0;
	prev_pt_left[0].y = 0;
	prev_pt_left[1].x = 0;
	prev_pt_left[1].y = 0;
	prev_pt_left[2].x = 0;
	prev_pt_left[2].y = 0;


	prev_pt_right[0].x = 0;
	prev_pt_right[0].y = 0;
	prev_pt_right[1].x = 0;
	prev_pt_right[1].y = 0;
	prev_pt_right[2].x = 0;
	prev_pt_right[2].y = 0;

	prev_prev_pt_left[0].x = 0;
	prev_prev_pt_left[0].y = 0;
	prev_prev_pt_left[1].x = 0;
	prev_prev_pt_left[1].y = 0;
	prev_prev_pt_left[2].x = 0;
	prev_prev_pt_left[2].y = 0;


	prev_prev_pt_right[0].x = 0;
	prev_prev_pt_right[0].y = 0;
	prev_prev_pt_right[1].x = 0;
	prev_prev_pt_right[1].y = 0;
	prev_prev_pt_right[2].x = 0;
	prev_prev_pt_right[2].y = 0;

}

void cross_product(int *a, int *b, int *result)
{
	int x = a[1] * b[2] - b[1] * a[2];
	int y = a[2] * b[0] - b[2] * a[0];
	int z = a[0] * b[1] - b[0] * a[1];

	result[0] = x;
	result[1] = y;
	result[2] = z;
}

void cross_product_f(float *a, float *b, float *result)
{
	float x = a[1] * b[2] - b[1] * a[2];
	float y = a[2] * b[0] - b[2] * a[0];
	float z = a[0] * b[1] - b[0] * a[1];

	result[0] = x;
	result[1] = y;
	result[2] = z;
}

void cal_bonnet(unsigned char *img, int width, int height)
{
	int i=0, j=0;

	for(i=0; i<height; i++)
	{
		for(j=0; j<width; j++)
		{
			mean_img[i*width+j] += ((float)img[i*width+j] / 800);
			square_mean_img[i*width+j] += ((float)img[i*width+j]*(float)img[i*width+j]) / 800;
			var_img[i*width+j] = square_mean_img[i*width+j] - mean_img[i*width+j]*mean_img[i*width+j];


			//printf("%f ", var_img[i*width+j]);
		}
	}
}

void draw_lane(unsigned char *img, int width, int height, int l_x, int l_y, int r_x, int r_y, int l_x2, int l_y2, int r_x2, int r_y2, PointKAIST *pt_left, PointKAIST *pt_right)//, int *prev_left, int *prev_right)
{
	int i=0, j=0;
	int left[3] = {0,}, right[3] = {0,};
	int left_s[3] = {0,}, right_s[3] = {0,};
	int left_line[3] = {0,}, right_line[3] = {0,};
	int y=0;

	left[0] = l_x;
	left[1] = l_y;
	left[2] = 1;

	right[0] = r_x;
	right[1] = r_y;
	right[2] = 1;

	//if(dist(l_x, l_x2) < 40)
	{
		left_s[0] = l_x2;
		left_s[1] = l_y2;
		left_s[2] = 1;

//		prev_left[0] = l_x2;
//		prev_left[1] = l_y2;
	}
	/*
	else
	{
		left_s[0] = prev_left[0];
		left_s[1] = prev_left[1];
		left_s[2] = 1;
	}
	*/

//	if(dist(r_x, r_x2) < 40)
	{
		right_s[0] = r_x2;
		right_s[1] = r_y2;
		right_s[2] = 1;

//		prev_right[0] = r_x2;
//		prev_right[1] = r_y2;
	}
	/*
	else
	{
		right_s[0] = prev_right[0];
		right_s[1] = prev_right[1];
		right_s[2] = 1;
	}
*/
	/**
	left_s[0] = 40;
	left_s[1] = height-1;
	left_s[2] = 1;

	right_s[0] = width-41;
	right_s[1] = height-1;
	right_s[2] = 1;
**/
	cross_product(left, left_s, left_line);
	cross_product(right, right_s, right_line);

	//printf("%d %d %d\t%d %d %d\n", left_line[0], left_line[1], left_line[2], right_line[0], right_line[1], right_line[2]);

	for(i=0; i<width; i++)
	{
		if(left_line[1] == 0)
			y = 0;
		else
			y = (int)((-left_line[0] * i - left_line[2]) / left_line[1]);

		//if(y >= height/2 + 20 && y < height)
		if(y >= 0 && y < height)
		{
			img[y*width+i] = 0;
			img[y*width+i+1] = 0;
			img[y*width+i+2] = 0;
		}
	}

	if(left_line[0] != 0)
	{
		pt_left[1].x = (int)((-left_line[1] * 300 - left_line[2]) / left_line[0]);
		pt_left[1].y = 300;
	}

	for(j=0; j<width; j++)
	{
		if(right_line[1] == 0)
			y = 0;
		else
			y = (int)((-right_line[0] * j - right_line[2]) / right_line[1]);

		
		
		//if(y >= height/2 + 20 && y < height)
		if(y >= 0 && y < height)
		{
			img[y*width+j] = 255;
			img[y*width+j+1] = 255;
			img[y*width+j+2] = 255;
		}

	}
	if(right_line[0] != 0)
	{
		pt_right[1].x = (int)((-right_line[1] * 300 - right_line[2]) / right_line[0]);
		pt_right[1].y = 300;
	}
}

void initialize_ldws_nev(int center_margin, int interval_size, int margin_l, int margin_r, int margin_b, int v_th, int h_center, int warning, int width, int height)
{
	int i=0;

	for(i=0; i<FILTER_NUM; i++)
	{
		margin[i] = center_margin;//(FILTER_NUM-1) - i*10;
		interval[i] = i*interval_size;
	}

	left_margin = margin_l;
	right_margin = margin_r;
	bottom_margin = margin_b;
	vanishing_th = v_th;
	horizontal_center = h_center;

	warning_val = warning;

}

void tracking(PointKAIST *p, PointKAIST *q, int width, int height)
{
	int i=0;
	double w1 = 0.8;
	double w2 = 0;
	
	w2 = 1-w1;

	for(i=0; i<3; i++)
	{
		if(mean_x[i] == 0)
		{
			mean_x[i] = p[i].x;
			mean_y[i] = p[i].y;
		}
		else
		{
			mean_x[i] = w1 * mean_x[i] + w2 * p[i].x;
			mean_y[i] = w1 * mean_y[i] + w2 * p[i].y;

		}

		//if(abs(mean_x2[i] - q[i].x) < 20)
		if(mean_x2[i] == 0)
		{
			mean_x2[i] = q[i].x;
			mean_y2[i] = q[i].y;
		}
		else
		{
			mean_x2[i] = w1 * mean_x2[i] + w2 * q[i].x;
			mean_y2[i] = w1 * mean_y2[i] + w2 * q[i].y;

		}
		p[i].x = mean_x[i];
		p[i].y = mean_y[i];
		q[i].x = mean_x2[i];
		q[i].y = mean_y2[i];
	}
}

int dist(int a, int b)
{
	return abs(a-b);
}

float mean(int *arr, int n)
{
	int i=0;
	float mean_val = 0;

	for(i=0; i<n; i++)
	{
		mean_val += (float)arr[i] / (float)n;
	}

	return mean_val;
}

float W(unsigned char *img, int i, int j, int k, int width, int height)
{
	return 1;
}

void ransac(unsigned char *img, PointKAIST *left, PointKAIST *right, int iter)
{
	int i=0, j=0;
	int p1_l[3], p2_l[3], p1_r[3], p2_r[3];
	int idx1_l=0, idx2_l=0;
	int idx1_r=0, idx2_r=0;
	int left_line[3] = {0,}, right_line[3] = {0,};
	int best_left_line[3] = {0,}, best_right_line[3] = {0,};
	int inlier_l = 0, inlier_r = 0;
	int best_inlier_l = 0, best_inlier_r = 0;
	int best_idx1_l=0, best_idx2_l=0;
	int best_idx1_r=0, best_idx2_r=0;
	int y=0;


	PointKAIST result_l[2], result_r[2];

	for(i=0; i<iter; i++)
	{	
		inlier_l = 0;
		inlier_r = 0;

		// 왼쪽 라인
		idx1_l = rand() % FILTER_NUM;
		idx2_l = rand() % FILTER_NUM;

		while(idx1_l == idx2_l)
		{
			idx2_l = rand() % FILTER_NUM;
		}

		if(idx1_l < idx2_l)
		{
			p1_l[0] = left[idx1_l].x;
			p1_l[1] = left[idx1_l].y;
			p1_l[2] = 1;

			p2_l[0] = left[idx2_l].x;
			p2_l[1] = left[idx2_l].y;
			p2_l[2] = 1;
		}
		else
		{
			p1_l[0] = left[idx2_l].x;
			p1_l[1] = left[idx2_l].y;
			p1_l[2] = 1;

			p2_l[0] = left[idx1_l].x;
			p2_l[1] = left[idx1_l].y;
			p2_l[2] = 1;
		}


		// 오른쪽 라인
		idx1_r = rand() % FILTER_NUM;
		idx2_r = rand() % FILTER_NUM;

		while(idx1_r == idx2_r)
		{
			idx2_r = rand() % FILTER_NUM;
		}

		if(idx1_r < idx2_r)
		{
			p1_r[0] = right[idx1_r].x;
			p1_r[1] = right[idx1_r].y;
			p1_r[2] = 1;

			p2_r[0] = right[idx2_r].x;
			p2_r[1] = right[idx2_r].y;
			p2_r[2] = 1;
		}
		else
		{
			p1_r[0] = right[idx2_r].x;
			p1_r[1] = right[idx2_r].y;
			p1_r[2] = 1;

			p2_r[0] = right[idx1_r].x;
			p2_r[1] = right[idx1_r].y;
			p2_r[2] = 1;
		}


		cross_product(p1_l, p2_l, left_line);
		cross_product(p1_r, p2_r, right_line);

		// 각각 몇개의 점이 inlier 인지 셈

		for(j=0; j<FILTER_NUM; j++)
		{
			// left 이상함
			//printf("left : %d %d\n", left[j].x, left[j].y);
			//printf("right : %d %d\n", right[j].x, right[j].y);

			if(left_line[1] != 0 && (abs((-left_line[0] * left[j].x - left_line[2]) / left_line[1] - left[j].y)) < 10 && left[j].x != 0)
			{
				inlier_l++;

				//printf("1\n");
			}

			if(right_line[1] != 0 && (abs((-right_line[0] * right[j].x - right_line[2]) / right_line[1] - right[j].y)) < 10 && right[j].x != 0)
			{
				inlier_r++;
			}
		}

		//if((abs(horizontal_center - (float)left_right[1] / (float)left_right[2]) < 10) && temp_pt_right[2].x - temp_pt_left[2].x > 0 && x_left < x_right)
		{
			if(best_inlier_l < inlier_l)
			{
				best_inlier_l = inlier_l;
				best_idx1_l = idx1_l;
				best_idx2_l = idx2_l;

				result_l[0].x = p1_l[0];
				result_l[0].y = p1_l[1];
				result_l[1].x = p2_l[0];
				result_l[1].y = p2_l[1];

				best_left_line[0] = left_line[0];
				best_left_line[1] = left_line[1];
				best_left_line[2] = left_line[2];
			}

			if(best_inlier_r < inlier_r)
			{
				best_inlier_r = inlier_r;
				best_idx1_r = idx1_r;
				best_idx2_r = idx2_r;

				result_r[0].x = p1_r[0];
				result_r[0].y = p1_r[1];
				result_r[1].x = p2_r[0];
				result_r[1].y = p2_r[1];

				best_right_line[0] = right_line[0];
				best_right_line[1] = right_line[1];
				best_right_line[2] = right_line[2];
			}

			//printf("inlier : %d %d\n", best_inlier_l, best_inlier_r);
		}
	}


	// 끝점이 센터에 오는가 체크
//	if(best_left_line[1] != 0)
//		y = (int)((-best_left_line[0] * width/2 - best_left_line[2]) / best_left_line[1]);
//	else
//		y = 0;


	// 왼쪽
	//if((y - horizontal_center) > -20 && (float)best_inlier_l / FILTER_NUM > 0.5)
	if((float)best_inlier_l / FILTER_NUM > 0.1)
	{
		left[0].x = result_l[0].x;
		left[0].y = result_l[0].y;
		left[2].x = result_l[1].x;
		left[2].y = result_l[1].y;

		prev_result_l[0].x = result_l[0].x;
		prev_result_l[0].y = result_l[0].y;
		prev_result_l[1].x = result_l[1].x;
		prev_result_l[1].y = result_l[1].y;
	}
	else
	{
		left[0].x = prev_result_l[0].x;
		left[0].y = prev_result_l[0].y;
		left[2].x = prev_result_l[1].x;
		left[2].y = prev_result_l[1].y;
	}



//	if(best_right_line[1] != 0)
//		y = (int)((-best_right_line[0] * width/2 - best_right_line[2]) / best_right_line[1]);
//	else
//		y = 0;


	// 오른쪽
	//if(y - horizontal_center > -20 && (float)best_inlier_r / FILTER_NUM > 0.6)
	if((float)best_inlier_r / FILTER_NUM > 0.1)
	{
		right[0].x = result_r[0].x;
		right[0].y = result_r[0].y;
		right[2].x = result_r[1].x;
		right[2].y = result_r[1].y;

		prev_result_r[0].x = result_r[0].x;
		prev_result_r[0].y = result_r[0].y;
		prev_result_r[1].x = result_r[1].x;
		prev_result_r[1].y = result_r[1].y;
	}
	else
	{
		right[0].x = prev_result_r[0].x;
		right[0].y = prev_result_r[0].y;
		right[2].x = prev_result_r[1].x;
		right[2].y = prev_result_r[1].y;
	}
}

void calibration_ransac(unsigned char *img, PointKAIST *left, PointKAIST *right, int iter)
{
	int i=0, j=0;
	int p1_l[3], p2_l[3], p1_r[3], p2_r[3];
	int idx1_l=0, idx2_l=0;
	int idx1_r=0, idx2_r=0;
	int left_line[3] = {0,}, right_line[3] = {0,};
	int best_left_line[3] = {0,}, best_right_line[3] = {0,};
	int inlier_l = 0, inlier_r = 0;
	int best_inlier_l = 0, best_inlier_r = 0;
	int best_idx1_l=0, best_idx2_l=0;
	int best_idx1_r=0, best_idx2_r=0;
	int y=0;

	PointKAIST result_l[2], result_r[2];

	for(i=0; i<iter; i++)
	{	
		inlier_l = 0;
		inlier_r = 0;

		// 왼쪽 라인
		idx1_l = rand() % FILTER_NUM;
		idx2_l = rand() % FILTER_NUM;

		while(idx1_l == idx2_l)
		{
			idx2_l = rand() % FILTER_NUM;
		}


		if(idx1_l < idx2_l)
		{
			p1_l[0] = left[idx1_l].x;
			p1_l[1] = left[idx1_l].y;
			p1_l[2] = 1;

			p2_l[0] = left[idx2_l].x;
			p2_l[1] = left[idx2_l].y;
			p2_l[2] = 1;
		}
		else
		{
			p1_l[0] = left[idx2_l].x;
			p1_l[1] = left[idx2_l].y;
			p1_l[2] = 1;

			p2_l[0] = left[idx1_l].x;
			p2_l[1] = left[idx1_l].y;
			p2_l[2] = 1;
		}


		// 오른쪽 라인
		idx1_r = rand() % FILTER_NUM;
		idx2_r = rand() % FILTER_NUM;

		while(idx1_r == idx2_r)
		{
			idx2_r = rand() % FILTER_NUM;
		}

		if(idx1_r < idx2_r)
		{
			p1_r[0] = right[idx1_r].x;
			p1_r[1] = right[idx1_r].y;
			p1_r[2] = 1;

			p2_r[0] = right[idx2_r].x;
			p2_r[1] = right[idx2_r].y;
			p2_r[2] = 1;
		}
		else
		{
			p1_r[0] = right[idx2_r].x;
			p1_r[1] = right[idx2_r].y;
			p1_r[2] = 1;

			p2_r[0] = right[idx1_r].x;
			p2_r[1] = right[idx1_r].y;
			p2_r[2] = 1;
		}


		cross_product(p1_l, p2_l, left_line);
		cross_product(p1_r, p2_r, right_line);

		// 각각 몇개의 점이 inlier 인지 셈

		for(j=0; j<FILTER_NUM; j++)
		{
			// left 이상함
			//printf("left : %d %d\n", left[j].x, left[j].y);
			//printf("right : %d %d\n", right[j].x, right[j].y);

			if(left_line[1] != 0 && (abs((-left_line[0] * left[j].x - left_line[2]) / left_line[1] - left[j].y)) < 5 && left[j].x != 0)
			{
				inlier_l++;

				//printf("1\n");
			}

			if(right_line[1] != 0 && (abs((-right_line[0] * right[j].x - right_line[2]) / right_line[1] - right[j].y)) < 5 && right[j].x != 0)
			{
				inlier_r++;
			}
		}

		//if((abs(horizontal_center - (float)left_right[1] / (float)left_right[2]) < 10) && temp_pt_right[2].x - temp_pt_left[2].x > 0 && x_left < x_right)
		{
			if(best_inlier_l < inlier_l)
			{
				best_inlier_l = inlier_l;
				best_idx1_l = idx1_l;
				best_idx2_l = idx2_l;

				result_l[0].x = p1_l[0];
				result_l[0].y = p1_l[1];
				result_l[1].x = p2_l[0];
				result_l[1].y = p2_l[1];

				best_left_line[0] = left_line[0];
				best_left_line[1] = left_line[1];
				best_left_line[2] = left_line[2];
			}

			if(best_inlier_r < inlier_r)
			{
				best_inlier_r = inlier_r;
				best_idx1_r = idx1_r;
				best_idx2_r = idx2_r;

				result_r[0].x = p1_r[0];
				result_r[0].y = p1_r[1];
				result_r[1].x = p2_r[0];
				result_r[1].y = p2_r[1];

				best_right_line[0] = right_line[0];
				best_right_line[1] = right_line[1];
				best_right_line[2] = right_line[2];
			}

			//printf("inlier : %d %d\n", best_inlier_l, best_inlier_r);
		}
	}


	// 끝점이 센터에 오는가 체크
	if(best_left_line[1] != 0)
		y = (int)((-best_left_line[0] * width/2 - best_left_line[2]) / best_left_line[1]);
	else
		y = 0;


	// 왼쪽
	if(abs(y - vertical_center) < vanishing_th && (float)best_inlier_l / FILTER_NUM > 0.1)
	{
		left[0].x = result_l[0].x;
		left[0].y = result_l[0].y;
		left[2].x = result_l[1].x;
		left[2].y = result_l[1].y;

		prev_result_l[0].x = result_l[0].x;
		prev_result_l[0].y = result_l[0].y;
		prev_result_l[1].x = result_l[1].x;
		prev_result_l[1].y = result_l[1].y;
	}
	else
	{
		left[0].x = prev_result_l[0].x;
		left[0].y = prev_result_l[0].y;
		left[2].x = prev_result_l[1].x;
		left[2].y = prev_result_l[1].y;
	}



	if(best_right_line[1] != 0)
		y = (int)((-best_right_line[0] * width/2 - best_right_line[2]) / best_right_line[1]);
	else
		y = 0;


	// 오른쪽
	if(abs(y - horizontal_center) < vanishing_th && (float)best_inlier_r / FILTER_NUM > 0.1)
	{
		right[0].x = result_r[0].x;
		right[0].y = result_r[0].y;
		right[2].x = result_r[1].x;
		right[2].y = result_r[1].y;

		prev_result_r[0].x = result_r[0].x;
		prev_result_r[0].y = result_r[0].y;
		prev_result_r[1].x = result_r[1].x;
		prev_result_r[1].y = result_r[1].y;
	}
	else
	{
		right[0].x = prev_result_r[0].x;
		right[0].y = prev_result_r[0].y;
		right[2].x = prev_result_r[1].x;
		right[2].y = prev_result_r[1].y;
	}
}


void search_lane(unsigned char *img, int width, int height, PointKAIST *left, PointKAIST *right)
{
	int i=0, j=0, k=0;
	int sum = 0, sum2 = 0, sum3 = 0, max_sum = -99999;
	int left_max_idx = 0, left_max_idx_out = 0, right_max_idx = 0, right_max_idx_out = 0;
	int start = 0;
	int result = 0, result_out = 0;
	int step = 0;
	int max_step = FILTER_NUM;
	int T = 5;
	int max_sum_out = -9999;

	int lane_th = 20;

	// max_step : 필터 개수
	for(step=0; step < max_step; step++)
	{
		max_sum = -9999;
		max_sum_out = -9999;
		right_max_idx = 0;
		right_max_idx_out = 0;
		start = 0;

		// integral image
		// 하나의 세로 라인의 합을 sum_buf에 저장함
		for(j=0; j<width; j++)
		{
			sum_buf[step][j] = 0;

			for(i = height - 1 - interval[step]; i > height - 1 - interval[step] - mask_size; i--)
			{
				sum_buf[step][j] += img[i*width+j] / mask_size_2;
			}

			//printf("%d ", sum_buf[step][j]);
		}

//		printf("%d %d\n", center+mask_size+margin[step], width-mask_size);
		for(j=center-mask_size+margin[step]; j<width-right_margin; j++)
		{
			sum = 0;
			sum2 = 0;
			sum3 = 0;

			for(k=0; k<mask_size; k++)
			{
				sum3 += sum_buf[step][j+k+mask_size];
				sum += sum_buf[step][j+k];
				sum2 += sum_buf[step][j+k-mask_size];;
			}

			result = sum-sum2;
			result_out = sum - sum3;

			

			if(result - max_sum > T)
			{
				//printf("%d ", result);
				max_sum = result;
				right_max_idx = j;
			}

			if(result_out - max_sum_out > T)
			{
				max_sum_out = result_out;
				right_max_idx_out = j;
			}

			img[i*width+j+k] = 255;
		}


		if(right_max_idx_out - right_max_idx < lane_th && right_max_idx_out - right_max_idx > 0)
		{
			right[step].x = right_max_idx;
			right[step].y = (height-1-mask_size - interval[step] + height-1 - interval[step]) / 2;
		}
		else
		{
			right[step].x = 0;
			right[step].y = 0;
		}

		//printf("right : %d\n", right[step].y);

		//if(max_sum > 50)

		for(i=height-1 - interval[step]; i>height-1-mask_size - interval[step]; i--)
		{
			for(j=right_max_idx+start; j<right_max_idx+mask_size+start; j++)
			{
				img[i*width+j] = 255;

				if(right_max_idx_out - right_max_idx < lane_th && right_max_idx_out - right_max_idx > 0)
					img[i*width+j] = 100;
			}

			for(j=right_max_idx_out+start; j<right_max_idx_out+mask_size+start; j++)
			{
				img[i*width+j] = 0;

				if(right_max_idx_out - right_max_idx < lane_th && right_max_idx_out - right_max_idx > 0)
					img[i*width+j] = 100;
			}

			//start--;
		}


		max_sum = -9999;
		max_sum_out = -9999;
		left_max_idx = 0;
		left_max_idx_out = 0;
		start = 0;

		T = 5;

		for(j=center-mask_size-margin[step]; j>left_margin; j--)
		{
			sum = 0;
			sum2 = 0;
			sum3 = 0;
			start = 0;

			for(k=start; k<mask_size+start; k++)
			{
				sum3 += sum_buf[step][j+k-mask_size];
				sum += sum_buf[step][j+k];
				sum2 += sum_buf[step][j+k+mask_size];
			}

			result = sum-sum2;
			result_out = sum - sum3;

			if(result - max_sum > T)
			{
				max_sum = result;
				left_max_idx = j;
			}

			if(result_out - max_sum_out > T)
			{
				max_sum_out = result_out;
				left_max_idx_out = j;
			}

			img[i*width+j+k] = 0;
		}

		start = 0;

		if(left_max_idx - left_max_idx_out < lane_th && left_max_idx - left_max_idx_out > 0)
		{
			left[step].x = left_max_idx;
			left[step].y = (height-1-mask_size - interval[step] + height-1 - interval[step]) / 2;
		}
		else
		{
			left[step].x = 0;
			left[step].y = 0;
		}
		
		//if(max_sum > 50)
		{
			
		for(i=height-1 - interval[step]; i>height-1-mask_size - interval[step]; i--)
		{
			for(j=left_max_idx+start; j<left_max_idx+mask_size+start; j++)
			{
				img[i*width+j] = 255;

				if(left_max_idx - left_max_idx_out < lane_th && left_max_idx - left_max_idx_out > 0)
					img[i*width+j] = 100;
			}

			for(j=left_max_idx_out+start; j<left_max_idx_out+mask_size+start; j++)
			{
				img[i*width+j] = 0;

				if(left_max_idx - left_max_idx_out < lane_th && left_max_idx - left_max_idx_out > 0)
					img[i*width+j] = 100;
			}
		}

		img[left[step].y*width+left[step].x] = 255;
		}
	}
}

void calibration_search_lane(unsigned char *img, int width, int height, PointKAIST *left, PointKAIST *right)
{
	int i=0, j=0, k=0;
	int sum = 0, sum2 = 0, sum3 = 0, max_sum = -99999;
	int left_max_idx = 0, right_max_idx = 0;
	int start = 0;
	int result = 0;
	int step = 0;
	int max_step = FILTER_NUM;
	int T = 0;

	for(step=0; step<max_step; step++)
	{
		max_sum = 0;
		right_max_idx = 0;
		start = 0;

		for(j=0; j<width; j++)
		{
			sum_buf_cal[step][j] = 0;

			for(i=height-1 - interval_cal[step]; i>height-1- mask_size - interval_cal[step]; i--)
			{
				sum_buf_cal[step][j] += img[i*width+j] / mask_size_2;
			}
		}

//		printf("%d %d\n", center+mask_size+margin[step], width-mask_size);
		for(j=center/**+margin[step]**/; j<width/**-50**/; j++)
		{
			sum = 0;
			sum2 = 0;
			sum3 = 0;
			start = 0;

			for(k=start; k<mask_size+start; k++)
			{
				sum3 += /*W(color,i,j,k, width, height)*/sum_buf_cal[step][j+k+mask_size];
				sum += /*W(color,i,j,k, width, height)*/sum_buf_cal[step][j+k];// / mask_size_2;// + edge[i*width+j+k] / (mask_size*mask_size);
				sum2 += /*W(color,i,j,k, width, height)*/sum_buf_cal[step][j+k-mask_size];// / mask_size_2;// + edge[i*width+j+k-(int)mask_size] / (mask_size*mask_size);
			}

			result = /*1/(float)log(float(dist(center, j)))**/(2*sum - (sum2+sum3));// - sum3);

			if(result - max_sum > T  && result > 0)
			{
				max_sum = result;
				right_max_idx = j;
				//img[i*width+j+k] = 255;
				T = result - max_sum;
			}
			img[i*width+j+k] = 255;
		}

		right[step].x = right_max_idx;
		right[step].y = (height-1-mask_size - interval_cal[step] + height - 1 - interval_cal[step]) / 2;


		/**
		//if(max_sum > 50)
		for(i=height-1 - interval_cal[step]; i>height-1-mask_size - interval_cal[step]; i--)
		{
			for(j=right_max_idx+start; j<right_max_idx+mask_size+start; j++)
			{
				//printf("%d ", right_max_idx);
				img[i*width+j] = 255;
			}
			//start--;
		}
		**/

		max_sum = 0;
		left_max_idx = 0;
		start = 0;

		T = 0;

		for(j=center-mask_size/**-margin[step]**/; j>0; j--)
		{
			sum = 0;
			sum2 = 0;
			sum3 = 0;
			start = 0;

			for(k=start; k<mask_size+start; k++)
			{
				sum3 += /*W(color,i,j,k, width, height)*/sum_buf_cal[step][j+k-mask_size];
				sum += /*W(color,i,j,k, width, height)*/sum_buf_cal[step][j+k];//img[i*width+j+k] / mask_size_2;// + edge[i*width+j+k] / (mask_size*mask_size);
				sum2 += /*W(color,i,j,k, width, height)*/sum_buf_cal[step][j+k+mask_size];//img[i*width+j+k+(int)mask_size] / mask_size_2;// + edge[i*width+j+k+(int)mask_size] / (mask_size*mask_size);
			}

			result = /*1/(float)log(float(dist(center, j)))**/(2*sum - (sum2+sum3));// - sum3);//1/(float)dist(center, j) * (sum - sum2);// - sum3);

			if(result - max_sum > T && result > 0)
			{
				max_sum = result;
				left_max_idx = j;
				img[i*width+j+k] = 0;
				T = result - max_sum;
			}
		}

		start = 0;

		left[step].x = left_max_idx;
		left[step].y = (height-1-mask_size - interval_cal[step] + height - 1 - interval_cal[step]) / 2;
		
		/**
		//if(max_sum > 50)
		{
		for(i=height-1 - interval_cal[step]; i>height-1-mask_size - interval_cal[step]; i--)
		{
			for(j=left_max_idx+start; j<left_max_idx+mask_size+start; j++)
			{
				img[i*width+j] = 0;
			}
			//start++;
		}
		
		}
		**/
	}
}

int warning_system(int center, int lane_center, int T, int left_x, int right_x)
{
	float ratio = 0;

	
	// 오른쪽
	if((center - lane_center) > T)
		return 1;

	// 왼쪽
	else if((lane_center - center) > T)
		return 2;
	else
		return 0;
}

int warning_system2(PointKAIST *left, PointKAIST *right)
{
	if(left[0].x - mean_x[0] > 30 && left[1].x - mean_x[1] > 20 && left[2].x - mean_x[2] > 10 &&
		right[0].x - mean_x2[0] > 30 && right[1].x - mean_x2[1] > 20 && right[2].x - mean_x2[2] > 10)
	{
		return 2;
	}

	if(left[0].x - mean_x[0] < -30 && left[1].x - mean_x[1] < -20 && left[2].x - mean_x[2] < -10 &&
		right[0].x - mean_x2[0] < -30 && right[1].x - mean_x2[1] < -20 && right[2].x - mean_x2[2] < -10)
	{
		return 1;
	}

	return 0;
}


void draw_warning(unsigned char *img, int width, int height)
{
	int i=0, j=0;

	for(i=height/2-50; i<height/2+50; i++)
	{
		for(j=width/2-50; j<width/2+50; j++)
		{
			img[i*width+j] = 255;
		}
	}
}

void draw_warning2(unsigned char *img, int width, int height)
{
	int i=0, j=0;
	for(i=height/2-50; i<height/2+50; i++)
	{
		for(j=width/2-50; j<width/2+50; j++)
		{
			img[i*width+j] = 0;
		}
	}
}

int is_road(unsigned char *img, int width, int height, int n)
{
	int center = width/2;
	int sum = 0, i=0, j=0;
	int num = 0;

	float result=0;

	for(i=height-1; i>height-1-n*2; i--)
	{
		for(j=center-n; j<center+n; j++)
		{
			sum += img[i*width+j];
			num++;
		}
	}

	result = (float)sum/(float)num;

	if(result > 5)
		return 0;
	else
		return 1;
}

void lane_filtering(PointKAIST *pt_left, PointKAIST *pt_right, int WIDTH, int HEIGHT)
{
	/////// point 저장 변수
	PointKAIST temp_pt_left[3] = {0,}, temp_pt_right[3] = {0,};
	PointKAIST temp_left, temp_right;
	int points_num = 0;
	int x_left=0, x_right=0;
	
	/////// line 파라미터 저장 변수
	int left[3], left_s[3], left_line[3];
	int right[3], right_s[3], right_line[3];
	int left_right[3];

	int r_length = 0, l_length = 0;
	
	left_s[0] = pt_left[0].x; left_s[1] = pt_left[0].y; left_s[2] = 1;
	left[0] = pt_left[2].x; left[1] = pt_left[2].y; left[2] = 1;
	right_s[0] = pt_right[0].x; right_s[1] = pt_right[0].y; right_s[2] = 1;
	right[0] = pt_right[2].x; right[1] = pt_right[2].y; right[2] = 1;

	cross_product(left, left_s, left_line);
	cross_product(right, right_s, right_line);

//	cross_product(left_line, right_line, left_right);

	// 위에 있는지 여부 확인
	//printf("%d ", abs((-left_line[0] * pt_left[1].x - left_line[2]) / left_line[1] - pt_left[1].y));

	
	/////if(left_line[1] != 0 && (abs((-left_line[0] * pt_left[1].x - left_line[2]) / left_line[1] - pt_left[1].y)) < 20)
	{
		if(left_line[0] != 0)
		{
			temp_pt_left[0].x = (-left_line[1] * (HEIGHT-1) -left_line[2]) / left_line[0];//pt_right[2];
			temp_pt_left[0].y = HEIGHT-1;//pt_right[2];
		}
		else
			temp_pt_left[0] = pt_left[0];

		if(left_line[0] != 0)
		{
			temp_pt_left[1].x = (-left_line[1] * (360) -left_line[2]) / left_line[0];//pt_right[2];
			temp_pt_left[1].y = 360;//pt_right[2];
		}
		else
			temp_pt_left[1] = pt_left[1];

		temp_pt_left[2] = pt_left[2];

		l_length = 120 - pt_left[1].x;
	}
	/////else
	/////{
		/////temp_pt_left[1] = prev_pt_left[1];
		/////temp_pt_left[2] = prev_pt_left[2];

		/////l_length = 120 - temp_pt_left[1].x;
	/////}

	/////if(right_line[1] != 0 && (abs((-right_line[0] * pt_right[1].x - right_line[2]) / right_line[1] - pt_right[1].y)) < 20)
	{
		if(right_line[0] != 0)
		{
			temp_pt_right[0].x = (-right_line[1] * (HEIGHT-1) -right_line[2]) / right_line[0];//pt_right[2];
			temp_pt_right[0].y = HEIGHT-1;//pt_right[2];
		}
		else
			temp_pt_right[0] = pt_right[0];

		if(right_line[0] != 0)
		{
			temp_pt_right[1].x = (-right_line[1] * (360) -right_line[2]) / right_line[0];//pt_right[2];
			temp_pt_right[1].y = 360;//pt_right[2];
		}
		else
			temp_pt_right[1] = pt_right[1];

		temp_pt_right[2] = pt_right[2];

		r_length = pt_right[1].x - 120;
	}
	/////else
	/////{
		/////temp_pt_right[1] = prev_pt_right[1];
		/////temp_pt_right[2] = prev_pt_right[2];

		/////r_length = temp_pt_right[1].x - 120;
	/////}

	if(temp_pt_left[0].x == 0)
	{
		temp_pt_left[0] = prev_pt_left[0];
		temp_pt_left[1] = prev_pt_left[1];
		temp_pt_left[2] = prev_pt_left[2];
	}
	if(temp_pt_right[0].x == 0)
	{
		temp_pt_right[0] = prev_pt_right[0];
		temp_pt_right[1] = prev_pt_right[1];
		temp_pt_right[2] = prev_pt_right[2];
	}
	
//	printf("%d %d %d\n", temp_pt_right[0].x, temp_pt_right[1].x, temp_pt_right[2].x);
	left_s[0] = temp_pt_left[0].x; left_s[1] = temp_pt_left[0].y; left_s[2] = 1;
	left[0] = temp_pt_left[2].x; left[1] = temp_pt_left[2].y; left[2] = 1;
	right_s[0] = temp_pt_right[0].x; right_s[1] = temp_pt_right[0].y; right_s[2] = 1;
	right[0] = temp_pt_right[2].x; right[1] = temp_pt_right[2].y; right[2] = 1;


	cross_product(left, left_s, left_line);
	cross_product(right, right_s, right_line);
	cross_product(left_line, right_line, left_right);

	if(left_line[0] != 0)
		x_left = (-left_line[1]*300-left_line[2]) / left_line[0];

	if(right_line[0] != 0)
		x_right = (-right_line[1]*300-right_line[2]) / right_line[0];

	if(x_left < 0)
		x_left = 0;
	else if(x_left >= WIDTH)
		x_left = WIDTH-1;

	if(x_right < 0)
		x_right = 0;
	else if(x_right >= WIDTH)
		x_right = WIDTH-1;
	
	//if(((horizontal_center - (float)left_right[1] / (float)left_right[2]) < -20) && temp_pt_right[2].x - temp_pt_left[2].x > 0 && x_left < x_right)
	{
		if(temp_pt_left[0].x < temp_pt_left[2].x)
		{
			prev_pt_left[0] = temp_pt_left[0];
			prev_pt_left[1] = temp_pt_left[1];
			prev_pt_left[2] = temp_pt_left[2];
		}
		if(temp_pt_right[0].x > temp_pt_right[2].x)
		{
			prev_pt_right[0] = temp_pt_right[0];
			prev_pt_right[1] = temp_pt_right[1];
			prev_pt_right[2] = temp_pt_right[2];
		}
/*
		prev_pt_left[2].x = x_left;
		prev_pt_left[2].y = 150;
		prev_pt_right[2].x = x_right;
		prev_pt_right[2].y = 150;
		**/
	}

	pt_left[0] = prev_pt_left[0];
	pt_left[1] = prev_pt_left[1];
	pt_left[2] = prev_pt_left[2];

	pt_right[0] = prev_pt_right[0];
	pt_right[1] = prev_pt_right[1];
	pt_right[2] = prev_pt_right[2];

	if((float)left_right[0] / (float)left_right[2] >= 200 && (float)left_right[0] / (float)left_right[2] < 440)
		center = (float)left_right[0] / (float)left_right[2];
	else
		center = vertical_center;
}

void lane_filtering2(unsigned char *img, PointKAIST *pt_left, PointKAIST *pt_right, int WIDTH, int HEIGHT)
{
	/////// point 저장 변수
	PointKAIST temp_pt_left[3] = {0,}, temp_pt_right[3] = {0,};
	PointKAIST temp_left, temp_right;
	int points_num = 0;
	int x_left=0, x_right=0;
	
	/////// line 파라미터 저장 변수
	float left[3], left_s[3], left_line[3];
	float right[3], right_s[3], right_line[3];
	float left_right[3];

	int r_length = 0, l_length = 0;
	
	left_s[0] = pt_left[0].x; left_s[1] = pt_left[0].y; left_s[2] = 1;
	left[0] = pt_left[2].x; left[1] = pt_left[2].y; left[2] = 1;
	right_s[0] = pt_right[0].x; right_s[1] = pt_right[0].y; right_s[2] = 1;
	right[0] = pt_right[2].x; right[1] = pt_right[2].y; right[2] = 1;

	cross_product_f(left, left_s, left_line);
	cross_product_f(right, right_s, right_line);

printf("coord : %f %f\n", left_line[0], left_line[1]);

//	cross_product(left_line, right_line, left_right);

	// 위에 있는지 여부 확인
	//printf("%d ", abs((-left_line[0] * pt_left[1].x - left_line[2]) / left_line[1] - pt_left[1].y));

	
	/////if(left_line[1] != 0 && (abs((-left_line[0] * pt_left[1].x - left_line[2]) / left_line[1] - pt_left[1].y)) < 20)
	{
		if(left_line[0] != 0)
		{
			temp_pt_left[0].x = (-left_line[1] * (HEIGHT-1) -left_line[2]) / left_line[0];//pt_right[2];
			temp_pt_left[0].y = HEIGHT-1;//pt_right[2];
		}
		else
			temp_pt_left[0] = pt_left[0];


		if(left_line[0] != 0)
		{
			temp_pt_left[1].x = (-left_line[1] * (horizontal_center) -left_line[2]) / left_line[0];//pt_right[2];
			temp_pt_left[1].y = horizontal_center;//pt_right[2];
		}
		else
			temp_pt_left[1] = pt_left[1];


		temp_pt_left[2] = pt_left[2];

		l_length = 120 - pt_left[1].x;
	}
	/////else
	/////{
		/////temp_pt_left[1] = prev_pt_left[1];
		/////temp_pt_left[2] = prev_pt_left[2];

		/////l_length = 120 - temp_pt_left[1].x;
	/////}

	/////if(right_line[1] != 0 && (abs((-right_line[0] * pt_right[1].x - right_line[2]) / right_line[1] - pt_right[1].y)) < 20)
	{
		if(right_line[0] != 0)
		{
			temp_pt_right[0].x = (-right_line[1] * (HEIGHT-1) -right_line[2]) / right_line[0];//pt_right[2];
			temp_pt_right[0].y = HEIGHT-1;//pt_right[2];
		}
		else
			temp_pt_right[0] = pt_right[0];

		if(right_line[0] != 0)
		{
			temp_pt_right[1].x = (-right_line[1] * (horizontal_center) -right_line[2]) / right_line[0];//pt_right[2];
			temp_pt_right[1].y = horizontal_center;//pt_right[2];
		}
		else
			temp_pt_right[1] = pt_right[1];

		temp_pt_right[2] = pt_right[2];

		r_length = pt_right[1].x - 120;
	}
	/////else
	/////{
		/////temp_pt_right[1] = prev_pt_right[1];
		/////temp_pt_right[2] = prev_pt_right[2];

		/////r_length = temp_pt_right[1].x - 120;
	/////}

	if(temp_pt_left[0].x == 0)
	{
		temp_pt_left[0] = prev_pt_left[0];
		temp_pt_left[1] = prev_pt_left[1];
		temp_pt_left[2] = prev_pt_left[2];
	}
	if(temp_pt_right[0].x == 0)
	{
		temp_pt_right[0] = prev_pt_right[0];
		temp_pt_right[1] = prev_pt_right[1];
		temp_pt_right[2] = prev_pt_right[2];
	}
	
//	printf("%d %d %d\n", temp_pt_right[0].x, temp_pt_right[1].x, temp_pt_right[2].x);
	left_s[0] = temp_pt_left[0].x; left_s[1] = temp_pt_left[0].y; left_s[2] = 1;
	left[0] = temp_pt_left[2].x; left[1] = temp_pt_left[2].y; left[2] = 1;
	right_s[0] = temp_pt_right[0].x; right_s[1] = temp_pt_right[0].y; right_s[2] = 1;
	right[0] = temp_pt_right[2].x; right[1] = temp_pt_right[2].y; right[2] = 1;


	cross_product_f(left, left_s, left_line);
	cross_product_f(right, right_s, right_line);
	cross_product_f(left_line, right_line, left_right);


//	left_right[0] = (float)left_right[0] / (float)left_right[2];
//	left_right[1] = (float)left_right[1] / (float)left_right[2];


	if(left_line[0] != 0)
		x_left = (-left_line[1]*300-left_line[2]) / left_line[0];

	if(right_line[0] != 0)
		x_right = (-right_line[1]*300-right_line[2]) / right_line[0];

	if(x_left < 0)
		x_left = 0;
	else if(x_left >= WIDTH)
		x_left = WIDTH-1;

	if(x_right < 0)
		x_right = 0;
	else if(x_right >= WIDTH)
		x_right = WIDTH-1;
	
	if((abs(horizontal_center - (float)left_right[1] / (float)left_right[2]) < vanishing_th) && temp_pt_right[2].x - temp_pt_left[2].x > 0 && x_left < x_right)
	{
		if(temp_pt_left[0].x < temp_pt_left[2].x)
		{
			prev_pt_left[0] = temp_pt_left[0];
			prev_pt_left[1] = temp_pt_left[1];
			prev_pt_left[2] = temp_pt_left[2];
		}
		if(temp_pt_right[0].x > temp_pt_left[2].x)
		{
			prev_pt_right[0] = temp_pt_right[0];
			prev_pt_right[1] = temp_pt_right[1];
			prev_pt_right[2] = temp_pt_right[2];
		}
/*
		prev_pt_left[2].x = x_left;
		prev_pt_left[2].y = 150;
		prev_pt_right[2].x = x_right;
		prev_pt_right[2].y = 150;
		**/
	}

	pt_left[0] = prev_pt_left[0];
	pt_left[1] = prev_pt_left[1];
	pt_left[2] = prev_pt_left[2];

	pt_right[0] = prev_pt_right[0];
	pt_right[1] = prev_pt_right[1];
	pt_right[2] = prev_pt_right[2];
}


void draw_test(unsigned char *img, int width, int height)
{
	int i=0, j=0;

	for(i=80; i<100; i++)
	{
		for(j=width/2-50; j<width/2+50; j++)
		{
			img[i*width+j] = 0;
		}
	}

	for(i=100; i<200; i++)
	{
		for(j=width/2-10; j<width/2+10; j++)
		{
			img[i*width+j] = 0;
		}
	}
}

float get_noise(unsigned char *img, int width, int height, int left_x, int right_x, int y)
{
	int i=0;
	float total = right_x - left_x;
	float mean = 0;

	for(i=left_x; i<right_x; i++)
	{
		mean += (img[y*height+i+1] - img[y*height+i]) / total;
	}

	return mean;
}


/*************** 추가된 부분 ******************/
static int calibration_frame = 0;

static int center_bin[64][48] = {0,};

void cal_max_position1(int *x, int *y)
{
	int i=0, j=0;
	int max_val = 0;

	for(i=0; i<64; i++)
	{
		for(j=0; j<48; j++)
		{
			if(max_val < center_bin[i][j])
			{
				max_val = center_bin[i][j];
				*x = i;
				*y = j;
			}
		}
	}
}

void calibration(PointKAIST *pt_left, PointKAIST *pt_right, int WIDTH, int HEIGHT)
{
/////// point 저장 변수
	PointKAIST temp_pt_left[3] = {0,}, temp_pt_right[3] = {0,};
	PointKAIST temp_left, temp_right;
	int points_num = 0;
	int x_left=0, x_right=0;
	
	/////// line 파라미터 저장 변수
	int left[3], left_s[3], left_line[3];
	int right[3], right_s[3], right_line[3];
	int left_right[3];

	int r_length = 0, l_length = 0;
	
	left_s[0] = pt_left[0].x; left_s[1] = pt_left[0].y; left_s[2] = 1;
	left[0] = pt_left[2].x; left[1] = pt_left[2].y; left[2] = 1;
	right_s[0] = pt_right[0].x; right_s[1] = pt_right[0].y; right_s[2] = 1;
	right[0] = pt_right[2].x; right[1] = pt_right[2].y; right[2] = 1;

	cross_product(left, left_s, left_line);
	cross_product(right, right_s, right_line);

	cross_product(left, left_s, left_line);
	cross_product(right, right_s, right_line);
	cross_product(left_line, right_line, left_right);

	int cal_center_x = 0, cal_center_y = 0;

	if(left_right[2] != 0)
	{
		cal_center_x = left_right[0] / left_right[2];
		cal_center_y = left_right[1] / left_right[2];
	}

	if(cal_center_x > 0 && cal_center_y > 0 && center_bin[cal_center_x / 10][cal_center_y / 10] < 10000)
	{
		center_bin[cal_center_x / 10][cal_center_y / 10]++;
	}

	int pos_x, pos_y;

	cal_max_position1(&pos_x, &pos_y);

	horizontal_center = pos_y*10+5;
	vertical_center = pos_x*10+5;
	center = vertical_center;
}

/**************** 여기까지 *******************/

int LDWS(unsigned char *img, int width, int height, PointKAIST *pt_left, PointKAIST *pt_right)
{

	int result = 0, result2 = 0, i = 0;

	///////////calibration(img, width, height, pt_left, pt_right);

	search_lane(img, width, height-bottom_margin, pt_left, pt_right);

	ransac(img, pt_left, pt_right, 10);

	lane_filtering(pt_left, pt_right, width, height);

	// 일정 프레임(현재 100)까지 calibration을 수행하도록 하는 구문
	if(calibration_frame < 100)
	{
		calibration(pt_left, pt_right, width, height);
		calibration_frame++;
	}

	//result = warning_system2(pt_left, pt_right);
	//tracking(pt_left, pt_right, width, height);


	if(pt_left[0].x == 0 && pt_left[0].y == 0)
		init(pt_left, pt_right);

	if(pt_right[0].x == 0 && pt_right[0].y == 0)
		init(pt_left, pt_right);

/**
	// 기준선 그리기
	for(i=0; i<width; i++)
	{
		img[horizontal_center*width+i-1] = 255;
		img[horizontal_center*width+i] = 255;
		img[horizontal_center*width+i+1] = 255;
	}

	for(i=0; i<height; i++)
	{
		img[i*width+vertical_center-1] = 255;
		img[i*width+vertical_center] = 255;
		img[i*width+vertical_center+1] = 255;
	}
**/
	//printf("noise : %f\n", get_noise(img, width, height, pt_left[1].x, pt_right[1].x, pt_left[1].y));
/*
	//printf("%d ", pt_left[0].x);
	if((pt_right[0].x - pt_left[0].x) > 300 && pt_right[0].x != 0 && pt_right[0].y != 0 && pt_left[0].x != 0 && pt_right[0].y != 0)
		// 한쪽만 움직인 경우는 제외함
	{
		if((prev_prev_pt_left[0].x - pt_left[0].x) != 0 && (prev_prev_pt_right[0].x - pt_right[0].x) != 0)
		//printf("%d %d\n", pt_left[0].x, pt_right[0].x);


		// vertical_center로 교체됨
			result = warning_system(vertical_center, (pt_right[0].x - pt_left[0].x) / 2 + pt_left[0].x, warning_val, pt_left[0].x, pt_right[0].x);
	}
	else
	{
		result = 0;
		init(pt_left, pt_right);
	}
*/
	result = warning_system(vertical_center, (pt_right[0].x - pt_left[0].x) / 2 + pt_left[0].x, warning_val, pt_left[0].x, pt_right[0].x);
	//draw_lane(img, width, height, pt_left[0].x, pt_left[0].y, pt_right[0].x, pt_right[0].y, pt_left[2].x, pt_left[2].y, pt_right[2].x, pt_right[2].y, pt_left, pt_right);

	// 오른쪽
	if(result==1)
	{
		// 튀는 경우 방지
		if(start == 0 && prev_warning > 0)
		{
			//draw_warning(img, width, height);
			result2 = result;

			init(pt_left, pt_right);
		}
		else
			result2 = 0;
	}

	// 왼쪽
	else if(result==2)
	{
		if(start == 0 && prev_warning > 0)
		{
			//draw_warning2(img, width, height);
			result2 = result;

			init(pt_left, pt_right);
		}
		else
			result2 = 0;
	}
	else
	{
		start = 0;
		result = 0;
		result2 = 0;
	}

	prev_prev_pt_left[0].x = pt_left[0].x;
	prev_prev_pt_left[0].y = pt_left[0].y;
	prev_prev_pt_left[1].x = pt_left[1].x;
	prev_prev_pt_left[1].y = pt_left[1].y;
	prev_prev_pt_left[2].x = pt_left[2].x;
	prev_prev_pt_left[2].y = pt_left[2].y;

	prev_prev_pt_right[0].x = pt_right[0].x;
	prev_prev_pt_right[0].y = pt_right[0].y;
	prev_prev_pt_right[1].x = pt_right[1].x;
	prev_prev_pt_right[1].y = pt_right[1].y;
	prev_prev_pt_right[2].x = pt_right[2].x;
	prev_prev_pt_right[2].y = pt_right[2].y;

//	draw_test(img, width, height);


	//printf("warning : %d %d\n", prev_warning, result);

	//오른쪽 차선 1
	if(prev_warning == 1 && result == 2)
	{
		//draw_warning(img, width, height);
		//init(pt_left, pt_right);
		return 1;
	}
	// 왼쪽 차선 2
	else if(prev_warning == 2 && result == 1)
	{
		//draw_warning2(img, width, height);
		//init(pt_left, pt_right);
		return 2;
	}

	prev_warning = result;

	return 0;
}
