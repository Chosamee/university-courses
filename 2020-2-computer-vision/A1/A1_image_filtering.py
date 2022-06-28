from A1_funcs import *
import numpy as np
import time


class ImageFiltering:
    def __init__(self, g1d_hor, g1d_ver, g2d, img, img_name):
        self.gaussian_1d_hor = g1d_hor
        self.gaussian_1d_ver = g1d_ver
        self.gaussian_2d = g2d
        self.img = img
        self.img_name = img_name

    def gaussian_filtering(self):
        img_row, img_col = self.img.shape

        result_img = np.zeros([img_row * 3, img_col * 3])

        size_sigma = [[5, 11, 17], [1, 6, 11]]
        text = [['5x5', '11x11', '17x17'], ['s=1', 's=6', 's=11']]

        for i in range(3):
            for j in range(3):
                kernel = get_gaussian_filter_2d(size_sigma[0][i], size_sigma[1][j])
                text_cpt = text[0][i] + text[1][j]
                font = cv2.FONT_HERSHEY_COMPLEX
                color = (0, 0, 0)

                f_img = cross_correlation_2d(self.img, kernel)
                result_img[i * img_row: (i + 1) * img_row, j * img_col: (j + 1) * img_col] = f_img
                cv2.putText(result_img, text_cpt, (j * img_col + 10, i * img_row + 30), font, 1, color)

        cv2.imwrite('./result/part_1_gaussian_filtered_{}.png'.format(self.img_name), result_img)
        cv2.imshow("gaussian_filtered_{}".format(self.img_name), result_img.astype("uint8"))
        cv2.waitKey(0)
        cv2.destroyAllWindows()

    def time_diff_cal(self):
        start_time = time.time()
        filter_1d_img = cross_correlation_1d(self.img, self.gaussian_1d_ver)
        filter_1d_img = cross_correlation_1d(filter_1d_img, self.gaussian_1d_hor)
        end_time = time.time()
        time_1d = end_time - start_time
        print("filename : {} / filter 1d time : ".format(self.img_name), time_1d)

        start_time = time.time()
        filter_2d_img = cross_correlation_2d(self.img, self.gaussian_2d)
        end_time = time.time()
        time_2d = end_time - start_time
        print("filename : {} / filter 2d time : ".format(self.img_name), time_2d)

        diff = np.abs(filter_2d_img - filter_1d_img)
        difference = np.sum(diff)
        print("filename : {} / difference : ".format(self.img_name), difference)

        cv2.imshow("difference_{}".format(self.img_name), diff.astype("uint8"))
        cv2.waitKey(0)

    def compare_correlation(self):
        self.time_diff_cal()


if __name__ == '__main__':
    print("1D Gaussian Kernel (5,1)\n", get_gaussian_filter_1d(5, 1))
    print("2D Gaussian Kernel (5,1)\n", get_gaussian_filter_2d(5, 1))

    gaussian_1d_hor = get_gaussian_filter_1d(17, 6)
    gaussian_1d_ver = np.transpose(gaussian_1d_hor)
    gaussian_2d = get_gaussian_filter_2d(17, 6)

    lenna = cv2.imread("./lenna.png", cv2.IMREAD_GRAYSCALE)
    shapes = cv2.imread("./shapes.png", cv2.IMREAD_GRAYSCALE)

    lenna_filtering = ImageFiltering(gaussian_1d_hor, gaussian_1d_ver, gaussian_2d, lenna, 'lenna')
    shapes_filtering = ImageFiltering(gaussian_1d_hor, gaussian_1d_ver, gaussian_2d, shapes, 'shapes')

    lenna_filtering.gaussian_filtering()
    shapes_filtering.gaussian_filtering()

    lenna_filtering.compare_correlation()
    shapes_filtering.compare_correlation()

