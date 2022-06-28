import cv2
import numpy as np
from A1_funcs import *
import time


def compute(img, img_name):
    time1 = time.time()
    img_mag, img_dir = compute_image_gradient(img)
    time2 = time.time()
    img_nms_mag = non_maximum_suppression_dir(img_mag, img_dir)
    end_time = time.time()

    print("input file : {} / gradient time : ".format(img_name), time2 - time1)
    print("input file : {} / NMS time:".format(img_name), end_time - time2)

    w_img_mag = 255*img_mag/np.max(img_mag)
    w_img_nms_mag = 255*img_nms_mag/np.max(img_nms_mag)

    cv2.imwrite("./result/part_2_edge_raw_{}.png".format(img_name), w_img_mag)
    cv2.imwrite("./result/part_2_edge_sup_{}.png".format(img_name), w_img_nms_mag)

    cv2.imshow("edge_raw_{}".format(img_name), w_img_mag.astype("uint8"))
    cv2.imshow("edge_sup_{}".format(img_name), w_img_nms_mag.astype("uint8"))
    cv2.waitKey(0)
    cv2.destroyAllWindows()


if __name__ == '__main__':
    gaussian = get_gaussian_filter_2d(7, 1.5)
    lenna_img = cv2.imread("./lenna.png", cv2.IMREAD_GRAYSCALE)
    shapes_img = cv2.imread("./shapes.png", cv2.IMREAD_GRAYSCALE)
    lenna = cross_correlation_2d(lenna_img, gaussian)
    shapes = cross_correlation_2d(shapes_img, gaussian)

    compute(lenna, 'lenna')
    compute(shapes, 'shapes')
