/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.experiment;

import static xyz.fycz.myreader.experiment.DoubleUtils.add;
import static xyz.fycz.myreader.experiment.DoubleUtils.div;
import static xyz.fycz.myreader.experiment.DoubleUtils.mul;
import static xyz.fycz.myreader.experiment.DoubleUtils.sub;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用最小二乘法实现线性回归预测
 *
 * @author fengyue
 * @date 2021/12/4 12:15
 */
public class LinearRegression {

    /**
     * 训练集数据
     */
    private Map<Double, Double> initData = new HashMap<>();

    /**
     * 截距
     */
    private double intercept = 0.0;
    //斜率
    private double slope = 0.0;

    /**
     * x、y平均值
     */
    private double averageX, averageY;
    /**
     * 求斜率的上下两个分式的值
     */
    private double slopeUp, slopeDown;


    public LinearRegression(Map<Double, Double> initData) {
        this.initData = initData;
        initData();
    }

    public LinearRegression() {
    }

    /**
     * 根据训练集数据进行训练预测
     * 并计算斜率和截距
     */
    public void initData() {
        if (initData.size() > 0) {
            //数据个数
            int number = 0;
            //x值、y值总和
            double sumX = 0;
            double sumY = 0;
            averageX = 0;
            averageY = 0;
            slopeUp = 0;
            slopeDown = 0;
            for (Double x : initData.keySet()) {
                if (x == null || initData.get(x) == null) {
                    continue;
                }
                number++;
                sumX = add(sumX, x);
                sumY = add(sumY, initData.get(x));
            }
            //求x，y平均值
            averageX = div(sumX, (double) number);
            averageY = div(sumY, (double) number);

            for (Double x : initData.keySet()) {
                if (x == null || initData.get(x) == null) {
                    continue;
                }

                slopeUp = add(slopeUp, mul(sub(x, averageX), sub(initData.get(x), averageY)));
                slopeDown = add(slopeDown, mul(sub(x, averageX), sub(x, averageX)));

            }
            initSlopeIntercept();
        }
    }

    /**
     * 计算斜率和截距
     */
    private void initSlopeIntercept() {
        if (slopeUp != 0 && slopeDown != 0) {
            slope = slopeUp / slopeDown;
        }
        intercept = averageY - averageX * slope;
    }

    /**
     * 根据x值预测y值
     *
     * @param x x值
     * @return y值
     */
    public Double getY(Double x) {
        return add(intercept, mul(slope, x));
    }

    /**
     * 根据y值预测x值
     *
     * @param y y值
     * @return x值
     */
    public Double getX(Double y) {
        return div(sub(y, intercept), slope);
    }


    public Map<Double, Double> getInitData() {
        return initData;
    }

    public void setInitData(Map<Double, Double> initData) {
        this.initData = initData;
    }

}
