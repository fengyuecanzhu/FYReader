package xyz.fycz.myreader.ai;

/**
 * @author fengyue
 * @date 2021/4/7 16:10
 */
public class MatrixUtil {
    //矩阵加法 C=A+B
    public static double[][] add(double[][] m1, double[][] m2) {
        if (m1 == null || m2 == null ||
                m1.length != m2.length ||
                m1[0].length != m2[0].length) {
            return null;
        }

        double[][] m = new double[m1.length][m1[0].length];

        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[i].length; ++j) {
                m[i][j] = m1[i][j] + m2[i][j];
            }
        }

        return m;
    }

    public static double[][] add(double[][] m, double a) {
        if (m == null) {
            return null;
        }

        double[][] retM = new double[m.length][m[0].length];

        for (int i = 0; i < retM.length; ++i) {
            for (int j = 0; j < retM[i].length; ++j) {
                retM[i][j] = m[i][j] + a;
            }
        }

        return retM;
    }

    public static double[][] sub(double[][] m1, double[][] m2) {
        if (m1 == null || m2 == null ||
                m1.length != m2.length ||
                m1[0].length != m2[0].length) {
            return null;
        }

        double[][] m = new double[m1.length][m1[0].length];

        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[i].length; ++j) {
                m[i][j] = m1[i][j] - m2[i][j];
            }
        }

        return m;
    }

    //矩阵转置
    public static double[][] transpose(double[][] m) {
        if (m == null) return null;
        double[][] mt = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[i].length; ++j) {
                mt[j][i] = m[i][j];
            }
        }
        return mt;
    }

    //矩阵相乘 C=A*B
    public static double[][] dot(double[][] m1, double[][] m2) {
        if (m1 == null || m2 == null || m1[0].length != m2.length)
            return null;

        double[][] m = new double[m1.length][m2[0].length];
        for (int i = 0; i < m1.length; ++i) {
            for (int j = 0; j < m2[0].length; ++j) {
                for (int k = 0; k < m1[i].length; ++k) {
                    m[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }

        return m;
    }

    //数乘矩阵
    public static double[][] dot(double[][] m, double k) {
        if (m == null) return null;
        double[][] retM = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                retM[i][j] = m[i][j] * k;
            }
        }
        return retM;
    }

    //同型矩阵除法
    public static double[][] divide(double[][] m1, double[][] m2) {
        if (m1 == null || m2 == null ||
                m1.length != m2.length ||
                m1[0].length != m2[0].length) {
            return null;
        }
        double[][] retM = new double[m1.length][m1[0].length];
        for (int i = 0; i < retM.length; ++i) {
            for (int j = 0; j < retM[i].length; ++j) {
                retM[i][j] = m1[i][j] / m2[i][j];
            }
        }
        return retM;
    }

    //矩阵除数
    public static double[][] divide(double[][] m, double k) {
        if (m == null) return null;
        double[][] retM = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                retM[i][j] = m[i][j] / k;
            }
        }
        return retM;
    }

    //求矩阵行列式（需为方阵）
    public static double det(double[][] m) {
        if (m == null || m.length != m[0].length)
            return 0;

        if (m.length == 1)
            return m[0][0];
        else if (m.length == 2)
            return det2(m);
        else if (m.length == 3)
            return det3(m);
        else {
            int re = 0;
            for (int i = 0; i < m.length; ++i) {
                re += (((i + 1) % 2) * 2 - 1) * det(companion(m, i, 0)) * m[i][0];
            }
            return re;
        }
    }

    //求二阶行列式
    public static double det2(double[][] m) {
        if (m == null || m.length != 2 || m[0].length != 2)
            return 0;

        return m[0][0] * m[1][1] - m[1][0] * m[0][1];
    }

    //求三阶行列式
    public static double det3(double[][] m) {
        if (m == null || m.length != 3 || m[0].length != 3)
            return 0;

        double re = 0;
        for (int i = 0; i < 3; ++i) {
            int temp1 = 1;
            for (int j = 0, k = i; j < 3; ++j, ++k) {
                temp1 *= m[j][k % 3];
            }
            re += temp1;
            temp1 = 1;
            for (int j = 0, k = i; j < 3; ++j, --k) {
                if (k < 0) k += 3;
                temp1 *= m[j][k];
            }
            re -= temp1;
        }

        return re;
    }

    //求矩阵的逆（需方阵）
    public static double[][] inv(double[][] m) {
        if (m == null || m.length != m[0].length)
            return null;

        double A = det(m);
        double[][] mi = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[i].length; ++j) {
                double[][] temp = companion(m, i, j);
                mi[j][i] = (((i + j + 1) % 2) * 2 - 1) * det(temp) / A;
            }
        }

        return mi;
    }

    //求方阵代数余子式
    public static double[][] companion(double[][] m, int x, int y) {
        if (m == null || m.length <= x || m[0].length <= y ||
                m.length == 1 || m[0].length == 1)
            return null;

        double[][] cm = new double[m.length - 1][m[0].length - 1];

        int dx = 0;
        for (int i = 0; i < m.length; ++i) {
            if (i != x) {
                int dy = 0;
                for (int j = 0; j < m[i].length; ++j) {
                    if (j != y) {
                        cm[dx][dy++] = m[i][j];
                    }
                }
                ++dx;
            }
        }
        return cm;
    }

    //生成全为0的矩阵
    public static double[][] zeros(int rows, int cols){
        return new double[rows][cols];
    }

    //生成全为1的矩阵
    public static double[][] ones(int rows, int cols){
        return add(zeros(rows, cols), 1);
    }

    public static double sum(double[][] matrix){
        double sum = 0;
        for (double[] doubles : matrix) {
            for (double aDouble : doubles) {
                sum += aDouble;
            }
        }
        return sum;
    }

    public static double[][] pow(double[][] matrix, int exponent){
        if (matrix == null) return null;
        double[][] retM = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                retM[i][j] = Math.pow(matrix[i][j], exponent);
            }
        }
        return retM;
    }

    public static double[][] sqrt(double[][] matrix){
        if (matrix == null) return null;
        double[][] retM = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                retM[i][j] = Math.sqrt(matrix[i][j]);
            }
        }
        return matrix;
    }

    public static double[][] to2dMatrix(double[] vector, boolean isCol){
        if (vector == null) return null;
        double[][] retM;
        if (isCol) {
            retM = new double[vector.length][1];
        }else {
            retM = new double[1][vector.length];
        }
        for (int i = 0; i < vector.length; i++) {
            if (isCol) {
                retM[i][0] = vector[i];
            }else {
                retM[0][i] = vector[i];
            }
        }
        return retM;
    }

    public static double[] toVector(double[][] matrix){
        double[] retV = null;
        if (matrix.length == 1){
            retV = new double[matrix[0].length];
            double[] doubles = matrix[0];
            System.arraycopy(doubles, 0, retV, 0, doubles.length);
        }else if (matrix[0].length == 1){
            retV = new double[matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                retV[i] = matrix[i][0];
            }
        }
        return retV;
    }
}
