import numpy as np
import cv2
from A2_funcs import *


def show_plane(plane):
    cv2.arrowedLine(plane, (400, 801), (400, 0), (0, 0, 0), thickness=2, tipLength=0.05)
    cv2.arrowedLine(plane, (0, 400), (801, 400), (0, 0, 0), thickness=2, tipLength=0.05)
    cv2.destroyAllWindows()
    cv2.imshow("smile", plane)
    return 0


def get_transformed_image(img, M):
    plane = np.ones((801, 801))

    # [x, y, 1] => [[[1,1,1,1,],[2,2,2,]...], [[1,2,3,4,],[1,2,3,4,]], [[1,1,1,][]]]
    # for 1-1 matching
    coor_matrix = np.ones((3, 801, 801))

    for i in range(801):
        coor_matrix[0][i] = i - 400
        coor_matrix[1][i] = np.arange(801) - 400

    M_inv = np.linalg.inv(M)
    coor_matrix = coor_matrix.reshape(3, 801*801).astype(int)
    origin_pos = np.dot(M_inv, coor_matrix).astype(int)
    coor_matrix += 400
    origin_pos += 400

    pos_all = list(zip(origin_pos[0], origin_pos[1]))
    coor = list(zip(coor_matrix[0], coor_matrix[1]))

    x_0 = np.where(origin_pos[0] > 0)
    x_row = np.where(origin_pos[0] < 801)
    y_0 = np.where(origin_pos[1] > 0)
    y_col = np.where(origin_pos[1] < 801)

    x_int = set(x_0[0]).intersection(set(x_row[0]))
    y_int = set(y_0[0]).intersection(set(y_col[0]))
    index_not_0 = x_int.intersection(y_int)
    for pos in list(index_not_0):
        plane[coor[pos][1], coor[pos][0]] = img[pos_all[pos][1], pos_all[pos][0]]

    return plane


if __name__ == '__main__':
    img = cv2.imread("smile.png", cv2.IMREAD_GRAYSCALE)
    img_row, img_col = img.shape

    plane = np.ones((801, 801))
    plane[400 - img_row//2 : 401 + img_row//2, 400 - img_col//2 : 401 + img_col//2] = img

    matrix = Matrix().dic
    M = np.identity(3)
    show_plane(plane.copy())

    while True:
        key = chr(cv2.waitKey(0))
        print(key)
        if key in 'adwsrRfFxXyY':
            M = np.dot(matrix[key], M)
            trans_plane = get_transformed_image(plane, M)
            show_plane(trans_plane.copy())

        elif key == 'H':
            M = np.identity(3)
            show_plane(plane)

        elif key == 'Q':
            cv2.destroyAllWindows()
            break