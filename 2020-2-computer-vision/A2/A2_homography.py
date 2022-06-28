import numpy as np
import cv2
import random
from A2_funcs import *

class A2_2_utils:
    def __init__(self, img1, img2, n):
        self.img1 = img1
        self.img2 = img2
        self.img1_img, self.img1_kp, self.img1_des = self.create_yoso(self.img1)
        self.img2_img, self.img2_kp, self.img2_des = self.create_yoso(self.img2)
        self.match = []
        self.n = n
        self.src = np.zeros([self.n, 2])
        self.dest = np.zeros([self.n, 2])

    def create_yoso(self, img):
        orb = cv2.ORB_create()
        kp = orb.detect(img, None)
        kp, des = orb.compute(img, kp)
        return img, kp, des

    def matching_img(self):
        for i in range(len(self.img1_des)):
            distance_min = cv2.norm(self.img1_des[i], self.img2_des[0], cv2.NORM_HAMMING)
            idx = (i, 0)

            for j in range(1, len(self.img2_des)):
                dist = cv2.norm(self.img1_des[i], self.img2_des[j], cv2.NORM_HAMMING)
                if distance_min > dist:
                    distance_min = dist
                    idx = (i, j)
            self.match.append(cv2.DMatch(idx[0], idx[1], distance_min))

        self.match.sort(key=lambda x: x.distance)
        res = None
        res = cv2.drawMatches(self.img1_img, self.img1_kp, self.img2_img, self.img2_kp, self.match[:10], res, flags=2)
        self.show_img(res, "..")
        return

    def show_img(self, img, img_name):
        cv2.imshow(img_name, img)
        cv2.waitKey(0)
        cv2.destroyAllWindows()

    def normalize(self, matrix):
        mean = matrix.mean(axis=0)
        matrix = matrix - mean
        longest_dist = np.max(np.linalg.norm(matrix, axis=1))
        normalized_matrix = matrix * np.sqrt(2) / longest_dist

        # T is transform linear for   mean of the point origin(0,0)
        T = np.identity(3)
        T[0][2], T[1][2] = -mean[0], -mean[1]
        T[0:2, :] = np.sqrt(2)*T[0:2, :] / longest_dist

        return normalized_matrix, T

    def compute_homography(self, srcP, destP):
        xs, Ts = self.normalize(srcP)
        xd, Td = self.normalize(destP)

        N = srcP.shape[0]
        A = np.zeros([2*N, 9])

        for i in range(N):
            x, y = xs[i]
            xx, yy = xd[i]
            A[2*i:2*i + 2, :] = np.array([[-x, -y, -1, 0, 0, 0, x * xx, y * xx, xx],
                                          [0, 0, 0, -x, -y, -1, x * yy, y * yy, yy]])

        u, sigma, vt = np.linalg.svd(A)

        # normal
        Hn = vt[-1].reshape(3, 3)
        if Hn[2][2] != 0:
            Hn = Hn / Hn[2][2]
        else:
            Hn = np.zeros([3, 3])

        Td_inverse = np.linalg.inv(Td)
        H = np.dot(Td_inverse, np.dot(Hn, Ts))
        return H

    def compute_homography_ransac(self, srcP, destP, th):
        N = srcP.shape[0]

        c_max = 0
        check_duplicate = []

        inliers = []

        cnt = 0
        while(cnt < 1000 and len(inliers) < 10):
            sample_index = random.sample(range(N), 4)
            sample_index.sort()
            if sample_index in check_duplicate:
                continue
            else:
                check_duplicate.append(sample_index)

            cnt += 1

            srcP_sample, destP_sample = [], []
            for i in range(4):
                srcP_sample.append(srcP[sample_index[i]])
                destP_sample.append(destP[sample_index[i]])

            H = self.compute_homography(np.array(srcP_sample), np.array(destP_sample))
            inliers_sample = []
            c = 0
            for j in range(N):
                # [x, y, 1]
                coor_srcP = np.array([[srcP[j][0]], [srcP[j][1]], [1]])
                coor_destP = np.array([[destP[j][0]], [destP[j][1]], [1]])

                sub_coor = coor_destP - np.dot(H, coor_srcP)
                dist = sub_coor[0][0]**2 + sub_coor[1][0]**2
                if dist < th**2:
                    c += 1
                    inliers_sample.append(j)

            if c_max < c:
                c_max = c
                inliers = inliers_sample.copy()

        srcP_new = np.zeros((c_max + 1, 2))
        destP_new = np.zeros((c_max + 1, 2))
        for k in range(len(inliers)):
            srcP_new[k] = srcP[inliers[k]]
            destP_new[k] = destP[inliers[k]]

        # new list dimension > 1, for norm axis 1
        srcP_new[-1] = srcP[0]
        destP_new[-1] = destP[0]
        return self.compute_homography(srcP_new, destP_new)

    def compute_src_dest(self):
        for i in range(self.n):
            self.src[i] = self.img2_kp[self.match[i].trainIdx].pt
            self.dest[i] = self.img1_kp[self.match[i].queryIdx].pt

    def image_warping_normalize(self, img1, img2):
        H = self.compute_homography(self.src, self.dest)
        warping_img = cv2.warpPerspective(img2, H, (img1.shape[1], img1.shape[0]))
        wraped_img = self.make_wraped_img(warping_img, img1)
        self.show_2_img("normalize", warping_img, wraped_img)
        return H

    def image_wrapping_RANSAC(self, img1, img2, th):
        H = self.compute_homography_ransac(self.src, self.dest, th)
        warping_img = cv2.warpPerspective(img2, H, (img1.shape[1], img1.shape[0]))
        wraped_img = self.make_wraped_img(warping_img, img1)
        self.show_2_img("RANSAC", warping_img, wraped_img)
        return H

    def make_wraped_img(self, warping_img, target_img):
        wraped_img = np.copy(target_img)
        for i in range(target_img.shape[0]):
            for j in range(target_img.shape[1]):
                if warping_img[i][j]:
                    wraped_img[i][j] = warping_img[i][j]
        return wraped_img

    def show_2_img(self, tags, img, imgg):
        cv2.imshow(tags, img.astype("uint8"))
        cv2.imshow(tags+"0", np.array(imgg).astype("uint8"))
        cv2.waitKey(0)
        cv2.destroyAllWindows()
        return

    def resize(self, img, target_img):
        return cv2.resize(img, (target_img.shape[1], target_img.shape[0]), interpolation=cv2.INTER_AREA)

    def image_stitching(self):
        H = self.image_wrapping_RANSAC(self.src, self.dest, 3)
        warping_img = cv2.warpPerspective(self.img2, H, (self.img2.shape[1] + self.img1.shape[1], self.img2.shape[0]))
        end_position = warping_img.shape[1]

        # try to cut black space -> fail
        warping_img = warping_img[:, :end_position]

        # not gradation
        img = np.copy(warping_img)
        img[:, :self.img1.shape[1]] = self.img1[:, :self.img1.shape[1]]
        self.show_img(img, "just stitch")

        # gradation
        for j in range(self.img1.shape[1] - 100, self.img1.shape[1]):
            warping_img[:, j] = warping_img[:, j]*((j - (self.img1.shape[1] - 100))/100)\
                                + self.img1[:, j]*((self.img1.shape[1] - j)/100)

        warping_img[:, :self.img1.shape[1] - 100] = self.img1[:, :self.img1.shape[1] - 100]
        self.show_img(warping_img, "gradation stitch")
        return


if __name__ == '__main__':
    desk = cv2.imread("cv_desk.png", cv2.IMREAD_GRAYSCALE)
    cover = cv2.imread("cv_cover.jpg", cv2.IMREAD_GRAYSCALE)
    hp_cover = cv2.imread("hp_cover.jpg", cv2.IMREAD_GRAYSCALE)

    util = A2_2_utils(desk, cover, 19)
    util.matching_img()

    util.compute_src_dest()
    util.image_warping_normalize(desk, cover)
    util.image_wrapping_RANSAC(desk, cover, 3)
    resized_hp_cover = util.resize(hp_cover, cover)
    util.image_wrapping_RANSAC(desk, resized_hp_cover, 3)

    left = cv2.imread("diamondhead-10.png", cv2.IMREAD_GRAYSCALE)
    right = cv2.imread("diamondhead-11.png", cv2.IMREAD_GRAYSCALE)

    last_util = A2_2_utils(left, right, 20)
    last_util.matching_img()
    last_util.compute_src_dest()
    last_util.image_wrapping_RANSAC(left, right, 3)
    last_util.image_stitching()