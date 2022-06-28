import cv2
import numpy as np
import random
from compute_avg_reproj_error import compute_avg_reproj_error
import time

red = (0, 0, 255)
green = (0, 255, 0)
blue = (255, 0, 0)
color_set = [red, green, blue]


class FMatrix:
    def __init__(self, img1_name, img2_name, matches_txt, msg):
        self.img1_name = img1_name
        self.img2_name = img2_name
        self.M = np.loadtxt(matches_txt)
        self.F = compute_F_mine(self.M)
        self.msg = msg

    def epipolar(self):
        img1_init = cv2.imread(self.img1_name)
        img2_init = cv2.imread(self.img2_name)
        row = self.M.shape[0]
        key = 0
        while chr(key) != 'q':
            sample_index = random.sample(range(row), 3)
            sample_coor = self.M[sample_index, :]
            img1, img2 = np.copy(img1_init), np.copy(img2_init)
            for i in range(3):
                x1, y1, x2, y2 = sample_coor[i, 0], sample_coor[i, 1], \
                                 sample_coor[i, 2], sample_coor[i, 3]
                coor1 = np.array([[x1], [y1], [1]])
                coor2 = np.array([[x2], [y2], [1]])

                # CV_8.ppt 53p
                l1 = np.dot(self.F.T, coor2)
                l2 = np.dot(self.F, coor1)

                cv2.circle(img1, (int(x1), int(y1)), 3, color_set[i], 2)
                cv2.circle(img2, (int(x2), int(y2)), 3, color_set[i], 2)
                draw_line(img1, l1, color_set[i])
                draw_line(img2, l2, color_set[i])

            cv2.imshow(self.msg, cv2.hconcat([img1, img2]))
            key = cv2.waitKey(0)
            cv2.destroyAllWindows()

    def print_error(self):
        print("Average Reprojection Errors ({} and {})".format(self.img1_name, self.img2_name))
        print("\tRaw =", compute_avg_reproj_error(self.M, compute_F_raw(self.M)))
        print("\tNorm =", compute_avg_reproj_error(self.M, compute_F_norm(self.M)))
        print("\tMine =", compute_avg_reproj_error(self.M, self.F), "\n")


def draw_line(img, line, color):
    # ax + by + c = 0 => (-a/b)x - (c/b) = y
    a, b, c = line
    # start_point => x = 0,  end_point => x = img.shape[0]
    x1 = 0
    x2 = img.shape[1]
    y1 = int((-a / b) * x1 - (c / b))
    y2 = int((-a / b) * x2 - (c / b))
    return cv2.line(img, (x1, y1), (x2, y2), color, 1)


def compute_F_raw(M):
    row = M.shape[0]
    x1, y1, x2, y2 = M[:, 0], M[:, 1], M[:, 2], M[:, 3]
    # page 63 – 70 of 'CV_08_Two-View_Geometry.pdf'
    matrix = np.ones((row, 9))
    matrix[:, 0] = x1 * x2
    matrix[:, 1] = x1 * y2
    matrix[:, 2] = x1
    matrix[:, 3] = y1 * x2
    matrix[:, 4] = y1 * y1
    matrix[:, 5] = y1
    matrix[:, 6] = x2
    matrix[:, 7] = y2

    u, sigma, vt = np.linalg.svd(matrix)
    # elements of column of Vt, corresponding to the least singular value
    # S diagonal matrix, S[3, 3] min. => last = vt[-1]
    F = vt[-1].reshape((3, 3))
    return F


def compute_F_norm(M):
    # to make origin (0, 0)
    # similar A2_homography normalize
    mean = M.mean(axis=0)
    matrix = M - mean
    max_coor = np.max(abs(matrix))
    # for scaling to fit the image into an unit square [ (−1, −1), (+1, +1) ]
    M_norm = matrix / max_coor

    # T is transform linear for mean of the point origin(0,0)
    T1 = make_matrix_T(mean[:2], max_coor)
    T2 = make_matrix_T(mean[2:], max_coor)

    fmat = compute_F_raw(M_norm)
    u, sigma, vt = np.linalg.svd(fmat)

    # one of the diagonal should be zero
    sigma[2] = 0

    fmat_sol = np.dot(np.dot(u, np.diag(sigma)), vt)
    F = np.dot(np.dot(T2.T, fmat_sol), T1)
    return F


def make_matrix_T(mean, max_coor):
    T = np.identity(3)
    T[0][2], T[1][2] = -mean[0], -mean[1]
    T[0:2, :] = T[0:2, :] / max_coor
    return T


def compute_F_mine(M):
    # Use RANSAC, repeat to find F which minimize error
    # store sample sets, check duplicate
    check_duplicate = []
    row = M.shape[0]
    F_ret = np.array([])
    error_min = 100
    start = time.time()
    while time.time() - start <= 2.8:
        # eight-point algorithm, select random 8 points
        sample_index = sorted(random.sample(range(row), 8))
        if sample_index in check_duplicate:
            continue
        check_duplicate.append(sample_index)
        sample_coor = M[sample_index, :]
        F = compute_F_norm(sample_coor)
        error = compute_avg_reproj_error(M, F)
        if error < error_min:
            error_min = error
            F_ret = F
    return F_ret


if __name__ == '__main__':
    temple = FMatrix('temple1.png', 'temple2.png', 'temple_matches.txt', 'temple')
    temple.print_error()

    house = FMatrix('house1.jpg', 'house2.jpg', 'house_matches.txt', 'house')
    house.print_error()

    library = FMatrix('library1.jpg', 'library2.jpg', 'library_matches.txt', 'library')
    library.print_error()

    temple.epipolar()
    house.epipolar()
    library.epipolar()
