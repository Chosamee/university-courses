import numpy as np
import cv2
from sklearn.cluster import MiniBatchKMeans
import struct

if __name__ == '__main__':
    N = 1000
    cluster = 1000
    sift_set = np.ndarray([N], dtype=np.object)
    vector_num = []
    for i in range(N):
        sift_set[i] = np.fromfile("./sift/sift10" + str(i).zfill(4),
                                  dtype=np.uint8).astype(np.int16).reshape(-1, 128)
        vector_num.append(sift_set[i].shape[0])

    features = np.vstack(sift_set).astype(np.double)
    kmeans = MiniBatchKMeans(n_clusters=cluster, batch_size=200, verbose=10).fit(features)

    histogram = []

    for i in range(N):
        histo = np.zeros(cluster)
        vnum = vector_num[i]
        for des in sift_set[i]:
            idx = kmeans.predict([des])
            histo[idx] += 1/vnum
        histogram.append(histo)

    file = open("A4_2017313260.des", 'wb')
    file.write(struct.pack('ii', N, cluster))

    for i in range(N):
        for j in range(cluster):
            file.write(struct.pack('f', histogram[i][j]))
    file.close()
