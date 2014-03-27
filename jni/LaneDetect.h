#include <opencv/cv.h>
#include <opencv/highgui.h>
#include <opencv/cxcore.h>
#include <opencv/cvaux.h>

#include <vector>
#include <math.h>
//#include "RansacVp.h"
//#include "../common.h"
#ifndef M_PI
#define M_PI		3.14159265358979323846	// pi 
#define M_PIf		3.14159265358979323846f	// pi 
#define M_PI_2		1.57079632679489661923 	// pi/2 
#endif

#ifndef LANEDETECT
#define _RAD2DEG	(180./M_PI)
#define _DEG2RAD	(M_PI/180.)

using namespace std;

class CLaneDetect
{
public:
	int LaneDetect(IplImage *src, IplImage *ipm, IplImage *dest,CvMat *mat, int mode);

private:
	void LaneCandidate (IplImage *mag, IplImage *ori, IplImage *dst);
	//sPoint VanishingPoint (vector<sLine> &lane_cand, vector<sLine> *inlier);
	double DeltaRad (double ang1, double ang2);

//private:
	//sPoint _vp;	// 이전 스탭에서 계산된 vanishing point
};

void add_score(int idx, float value);
void shift_right();
void shift_left();
int max_score();
void set_number_of_lane(int l);
float get_likelihood(int lane);


struct sPoint {
	int x, y;

	sPoint() : x(0), y(0) { }
	sPoint(int x_, int y_) : x(x_), y(y_) { }
};

struct sLine {
	int sx, sy;
	int ex, ey;
	int dx, dy;
	double len;


	sLine() : sx(0), sy(0), ex(0), ey(0), dx(0), dy(0), len(0.) { }
	sLine(int sx_, int sy_, int ex_, int ey_) 
		: sx(sx_), sy(sy_), ex(ex_), ey(ey_) 
	{
		dx = ex - sx;
		dy = ey - sy;
		len = sqrt((double)(dx*dx + dy*dy));
	}
};

int DoLaneDetect(IplImage *src, int mode, int start);
int Max(const int *Numbers, const int Count);


#endif
