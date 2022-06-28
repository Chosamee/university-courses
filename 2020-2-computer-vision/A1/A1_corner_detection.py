import cv2
import numpy as np
from A1_funcs import *
import time


class CornerDetection:
    def __init__(self, gauss, img, img_name):
        self.gaussian = gauss
        self.img = img
        self.img_name = img_name

    def corner(self):
        img_row, img_col = self.img.shape
        img = cross_correlation_2d(self.img, self.gaussian)
        time1 = time.time()
        R_img = compute_corner_response(img)
        time2 = time.time()
        suppressed_R_img = non_maximum_suppression_win(R_img, 11)
        end_time = time.time()
        print("input file : {} / corner response time : ".format(self.img_name), time2 - time1)
        print("input file : {} / NMS time: ".format(self.img_name), end_time - time2)

        R_img_cv = cv2.cvtColor(img.astype('uint8'), cv2.COLOR_GRAY2BGR)

        for i in range(img_row):
            for j in range(img_col):
                if R_img[i][j] > 0.1:
                    R_img_cv = cv2.line(R_img_cv, (j, i), (j, i), (0, 255, 0))

        suppressed_R_img_cv = cv2.cvtColor(img.astype('uint8'), cv2.COLOR_GRAY2BGR)

        for i in range(img_row):
            for j in range(img_col):
                if suppressed_R_img[i][j] > 0.1:
                    cv2.circle(suppressed_R_img_cv, (j, i), 3, (0, 255, 0))

        cv2.imwrite("./result/part_3_corner_raw_{}.png".format(self.img_name), R_img * 255)
        cv2.imwrite("./result/part_3_corner_bin_{}.png".format(self.img_name), R_img_cv)
        cv2.imwrite("./result/part_3_corner_sup_{}.png".format(self.img_name), suppressed_R_img_cv)

        cv2.imshow("{} corner raw".format(self.img_name), R_img)
        cv2.imshow("{} corner bin".format(self.img_name), R_img_cv)
        cv2.imshow("{} corner sup:".format(self.img_name), suppressed_R_img_cv)
        cv2.waitKey(0)
        cv2.destroyAllWindows()


if __name__ == '__main__':
    lenna = cv2.imread("./lenna.png", cv2.IMREAD_GRAYSCALE)
    shapes = cv2.imread("./shapes.png", cv2.IMREAD_GRAYSCALE)
    gaussian = get_gaussian_filter_2d(7, 1.5)

    lenna_corner = CornerDetection(gaussian, lenna, 'lenna')
    shapes_corner = CornerDetection(gaussian, shapes, 'shapes')
    lenna_corner.corner()
    shapes_corner.corner()
