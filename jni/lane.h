#define FILTER_NUM 10

typedef struct PointKAIST
{
    int x;
    int y;
}PointKAIST;

void initialize_ldws_nev(int center_margin, int interval_size, int margin_l, int margin_r, int margin_b, int v_th, int h_center, int warning, int width, int height);
int LDWS(unsigned char *img, int width, int height, PointKAIST *pt_left, PointKAIST *pt_right);
void draw_test(unsigned char *img, int width, int height);
int AUTO_CALIBRATION(unsigned char *img, int width, int height, PointKAIST *pt_left, PointKAIST *pt_right);
