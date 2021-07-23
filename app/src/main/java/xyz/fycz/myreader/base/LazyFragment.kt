package xyz.fycz.myreader.base

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * @author fengyue
 * @date 2021/7/22 11:46
 */
abstract class LazyFragment : Fragment() {
    protected var mDisposable: CompositeDisposable? = null
    /**
     * 是否执行懒加载
     */
    private var isLoaded = false

    /**
     * 当前Fragment是否对用户可见
     */
    private var isVisibleToUser = false

    /**
     * 当使用ViewPager+Fragment形式会调用该方法时，setUserVisibleHint会优先Fragment生命周期函数调用，
     * 所以这个时候就,会导致在setUserVisibleHint方法执行时就执行了懒加载，
     * 而不是在onResume方法实际调用的时候执行懒加载。所以需要这个变量
     */
    private var isCallResume = false

    /**
     * 是否调用了setUserVisibleHint方法。处理show+add+hide模式下，默认可见 Fragment 不调用
     * onHiddenChanged 方法，进而不执行懒加载方法的问题。
     */
    private var isCallUserVisibleHint = false

    protected open fun addDisposable(d: Disposable?) {
        if (mDisposable == null) {
            mDisposable = CompositeDisposable()
        }
        mDisposable!!.add(d!!)
    }

    override fun onResume() {
        super.onResume()
        isCallResume = true
        if (!isCallUserVisibleHint) isVisibleToUser = !isHidden
        judgeLazyInit()
    }


    private fun judgeLazyInit() {
        if (!isLoaded && isVisibleToUser && isCallResume) {
            lazyInit()
            isLoaded = true
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isVisibleToUser = !hidden
        judgeLazyInit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
        isVisibleToUser = false
        isCallUserVisibleHint = false
        isCallResume = false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
        isCallUserVisibleHint = true
        judgeLazyInit()
    }

    override fun onDetach() {
        super.onDetach()
        if (mDisposable != null) {
            mDisposable!!.clear()
        }
    }
    abstract fun lazyInit()
}