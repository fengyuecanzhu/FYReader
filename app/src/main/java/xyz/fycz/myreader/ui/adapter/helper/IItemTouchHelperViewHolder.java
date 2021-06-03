package xyz.fycz.myreader.ui.adapter.helper;

/**
 * @author fengyue
 * @date 2021/6/3 17:47
 */
public interface IItemTouchHelperViewHolder {

    /**
     * item被选中，在侧滑或拖拽过程中更新状态
     */
    void onItemSelected();

    /**
     * item的拖拽或侧滑结束，恢复默认的状态
     */
    void onItemClear();
}
