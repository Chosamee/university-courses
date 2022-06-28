import numpy as np
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import FunctionTransformer
from sklearn.preprocessing import PolynomialFeatures


def kernel(x_):
    xnew = np.hstack([np.cos(np.pi*x_)])
    return xnew


def model1(x_, y_):
    xx = FunctionTransformer(kernel).fit_transform(x_)
    coef_output(xx, y_)


def model2(x_, y_):
    poly_features = PolynomialFeatures(degree=2, include_bias=False)
    xx = poly_features.fit_transform(x_)
    coef_output(xx, y_)


def coef_output(x_, y_):
    model = LinearRegression()
    model.fit(x_, y_)
    print('constant {}'.format(model.intercept_))
    print('coef {}'.format(model.coef_))


if __name__ == '__main__':
    data = np.array([[-1, 1], [0, 1], [1, 1], [1, 0]])

    x, y = data[:, 0], data[:, 1]
    x = x.reshape((len(x), 1))
    y = y.reshape((len(y), 1))

    print('f1')
    model1(x, y)
    print('f2')
    model2(x, y)
