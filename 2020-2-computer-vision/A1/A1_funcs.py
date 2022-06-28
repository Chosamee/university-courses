import cv2
import numpy as np


def cross_correlation_1d(img, kernel):
    img_row, img_col = img.shape
    ker_row, ker_col = kernel.shape
    pd_row, pd_col = ker_row//2, ker_col//2

    f = np.zeros((img_row, img_col))

    # horizontal
    if ker_row == 1:
        padding = np.zeros((img_row, img_col + ker_col - 1))
        padding[:img_row, pd_col:-pd_col] = img
        padding[:img_row, :pd_col] = img[:img_row, :1]
        padding[:img_row, -pd_col:] = img[:img_row, -1:]

        for i in range(img_row):
            for j in range(img_col):
                f[i][j] = np.sum(padding[i:i + 1, j:j + ker_col] * kernel)

    else:
        padding = np.zeros((img_row + ker_row - 1, img_col))
        padding[pd_row:-pd_row, :img_col] = img
        padding[:pd_row, :img_col] = img[:1, :img_col]
        padding[-pd_row:, :img_col] = img[-1:, :img_col]

        for i in range(img_row):
            for j in range(img_col):
                f[i][j] = np.sum(padding[i:i + ker_row, j:j + 1] * kernel)

    return f


def cross_correlation_2d(img, kernel):
    img_row, img_col = img.shape
    ker_row, ker_col = kernel.shape
    pd_row, pd_col = ker_row//2, ker_col//2

    f = np.zeros((img_row, img_col))

    padding = np.zeros((img_row + ker_row - 1, img_col + ker_col - 1))
    padding[pd_row:-pd_col, pd_col:-pd_col] = img

    padding[:pd_row, :pd_col] = img[0][0]
    padding[:pd_row, -pd_col:] = img[0][-1]
    padding[-pd_row:, :pd_col] = img[-1][0]
    padding[-pd_row:, -pd_col:] = img[-1][-1]

    padding[:pd_row, pd_col:-pd_col] = img[:1, :img_col]
    padding[-pd_row:, pd_col:-pd_col] = img[-1:, :img_col]
    padding[pd_row:-pd_row, :pd_col] = img[:img_row, :1]
    padding[pd_row:-pd_row, -pd_col:] = img[:img_row, -1:]

    for i in range(img_row):
        for j in range(img_col):
            f[i][j] = np.sum(padding[i:i + ker_row, j:j + ker_col] * kernel)
    return f


def get_gaussian_filter_1d(size, sigma):
    f_hor = np.zeros((1, size))
    for i in range(size):
        x = i - size//2
        f_hor[0][i] = np.exp(-(x**2)/(2*(sigma**2))) / (np.sqrt(2*np.pi) * sigma)
    f_hor = f_hor/np.sum(f_hor)
    return f_hor


def get_gaussian_filter_2d(size, sigma):
    f = np.zeros((size, size))
    for i in range(size):
        for j in range(size):
            x = i - size//2
            y = j - size//2
            f[i][j] = np.exp(-(x**2 + y**2) / (2*(sigma**2))) / (2*np.pi*sigma**2)
    f = f/np.sum(f)
    return f


def compute_image_gradient(img):
    sx = np.array([[-1, 0, 1], [-2, 0, 2], [-1, 0, 1]])
    sy = np.transpose(sx)
    fx = cross_correlation_2d(img, sx)
    fy = cross_correlation_2d(img, sy)
    mag = np.sqrt(fx * fx + fy * fy)
    dir = np.arctan2(fy, fx)
    return mag, dir


def non_maximum_suppression_dir(mag, input_dir):
    direc = ((input_dir + np.pi/8)//(np.pi/4)) % 4
    direc = direc.astype('int32')
    xy = [[0, 1], [1, 1], [1, 0], [1, -1]]
    mag_row, mag_col = mag.shape
    ret = np.zeros((mag_row, mag_col))
    padding = np.zeros((mag_row + 2, mag_col + 2))
    padding[1:-1, 1:-1] = mag

    for i in range(mag_row):
        for j in range(mag_col):
            if padding[i + 1][j + 1] >= padding[i + 1 + xy[direc[i][j]][0]][j + 1 + xy[direc[i][j]][1]]:
                if padding[i + 1][j + 1] >= padding[i + 1 - xy[direc[i][j]][0]][j + 1 - xy[direc[i][j]][1]]:
                    ret[i][j] = mag[i][j]
    return ret


def compute_corner_response(img):
    img_row, img_col = img.shape
    sx = np.array([[-1, 0, 1], [-2, 0, 2], [-1, 0, 1]])
    sy = np.transpose(sx)
    fx = cross_correlation_2d(img, sx)
    fy = cross_correlation_2d(img, sy)

    ixx = fx*fx
    iyy = fy*fy
    ixy = fx*fy

    w = np.ones((5, 5))

    m11 = cross_correlation_2d(ixx, w)
    m12 = cross_correlation_2d(ixy, w)
    m22 = cross_correlation_2d(iyy, w)

    R = (m11*m22 - m12*m12) - 0.04*((m11+m22)*(m11+m22))
    R = abs(R*(R > 0))
    R = R / np.max(R)

    return R


def non_maximum_suppression_win(R, winSize):
    R_row, R_col = R.shape
    s = winSize // 2
    ret = np.copy(R)

    padding = np.zeros((R_row + s, R_col + s))
    padding[s:R_row + s, s:R_col + s] = R

    for i in range(R_row):
        for j in range(R_col):
            w = padding[i:i + winSize, j:j + winSize]
            if R[i][j] != np.max(w):
                ret[i][j] = 0
    return ret
