package com.luhaibing.statelayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.R.attr.type;

/**
 * Created by luhaibing
 * <p>
 * Date: 2017-02-10.
 * Time: 00:18
 * <p>
 * className: Statelayout
 * classDescription: 多状态布局
 */
public class StateLayout extends FrameLayout {

    private static final String TAG = StateLayout.class.getSimpleName();

    private static final int NO_LAYOUT_ID = -1;
    /**
     * 动画的类别
     */
    public final static int TOGETHER = 1;
    public final static int SEQUENCE = 2;

    public final static int CONTENT = 0;
    public final static int LOADING = 1;
    public final static int ERROR = 2;
    public final static int EMPTY = 3;

    /**
     * 默认的参数
     */
    private static int DEFAULT_LOADING_LAYOUT_ID = NO_LAYOUT_ID;
    private static int DEFAULT_ERROR_LAYOUT_ID = NO_LAYOUT_ID;
    private static int DEFAULT_EMPTY_LAYOUT_ID = NO_LAYOUT_ID;
    private static boolean DEFAULT_USE_ANIM = false;
    private static int DEFAULT_ANIM_TYPE = TOGETHER;
    private static ViewAnims DEFAULT_VIEW_ANIMAS;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CONTENT, LOADING, ERROR, EMPTY})
    public @interface ViewKey {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TOGETHER, SEQUENCE})
    public @interface AnimType {
    }

    private SparseArray<View> mSparseArray = new SparseArray<>();
    private boolean mUseAnim;
    private boolean mAniming = false;
    private int mState;
    private int mAnimType;
    private ViewAnims mViewAnims = DEFAULT_VIEW_ANIMAS;
    private StateListener mStateListener;

    public static StateLayout wrap(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("activity can not be null.");
        }
        return wrap(((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0));
    }

    public static StateLayout wrap(Fragment fragment) {
        if (fragment == null) {
            throw new NullPointerException("fragment can not be null.");
        }
        View view = fragment.getView();
        if (view == null) {
            throw new NullPointerException("fragment must have view.");
        }
        return wrap(view);
    }

    public static StateLayout wrap(android.support.v4.app.Fragment fragment) {
        if (fragment == null) {
            throw new NullPointerException("fragment can not be null.");
        }
        View view = fragment.getView();
        if (view == null) {
            throw new NullPointerException("fragment must have view.");
        }
        return wrap(view);
    }

    public static StateLayout wrap(View view) {
        if (view == null) {
            throw new NullPointerException("view can not be null.");
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) {
            throw new RuntimeException("view must have parent view.");
        }
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        int index = parent.indexOfChild(view);
        parent.removeView(view);
        StateLayout layout = new StateLayout(view.getContext());
        //解决如果父节点为LinearLayout,切节点view的宽或者高为空但layout_weight不为0
        FrameLayout.LayoutParams flps = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(flps);
        layout.addView(CONTENT, view);
        parent.addView(layout, index, lp);
        return layout;
    }

    public StateLayout(Context context) {
        this(context, null);
    }

    public StateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    public StateListener getStateListener() {
        return mStateListener;
    }

    public StateLayout setStateListener(StateListener stateListener) {
        mStateListener = stateListener;
        return this;
    }

    private void initialize(Context context, AttributeSet attrs) {
        Log.e(TAG, "initialize.....");
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StateLayout);
        int loadingId = a.getResourceId(R.styleable.StateLayout_loading, DEFAULT_LOADING_LAYOUT_ID);
        int errorId = a.getResourceId(R.styleable.StateLayout_error, DEFAULT_ERROR_LAYOUT_ID);
        int emptyId = a.getResourceId(R.styleable.StateLayout_empty, DEFAULT_EMPTY_LAYOUT_ID);
        mState = a.getInteger(R.styleable.StateLayout_state, CONTENT);
        mUseAnim = a.getBoolean(R.styleable.StateLayout_use_anim, DEFAULT_USE_ANIM);
        mAnimType = a.getInteger(R.styleable.StateLayout_anim_type, DEFAULT_ANIM_TYPE);
        a.recycle();
        Log.e("TAG", "mState -->>> " + mState);
        addView(LOADING, loadingId);
        addView(EMPTY, emptyId);
        addView(ERROR, errorId);
        //noinspection WrongConstant
        setShowView(mState);
    }

    private void setShowView(@ViewKey int key) {
        View view = mSparseArray.get(key);
        if (view != null) {
            view.setVisibility(VISIBLE);
        }
    }

    /**
     * 通过布局id添加对应的视图
     *
     * @param key
     * @param layoutId
     */
    public StateLayout addView(@ViewKey int key, @LayoutRes int layoutId) {
        if (layoutId != NO_LAYOUT_ID) {
            return addView(key, LayoutInflater.from(getContext()).inflate(layoutId, this, false));
        }
        return this;
    }

    /**
     * 添加对应的视图
     *
     * @param key
     * @param view
     */
    public StateLayout addView(@ViewKey int key, View view) {
        View v = mSparseArray.get(key);
        if (v != null) {
            if (key == LOADING) {
                Log.w(TAG, "you have already set a loading view and would be instead of this new one.");
            } else if (key == EMPTY) {
                Log.w(TAG, "you have already set a empty view and would be instead of this new one.");
            } else if (key == ERROR) {
                Log.w(TAG, "you have already set a error view and would be instead of this new one.");
            } else if (key == CONTENT) {
                Log.w(TAG, "you have already set a content view and would be instead of this new one.");
            }
        }
        removeView(v);
        mSparseArray.put(key, view);
        view.setTag(0);
        view.setVisibility(key == mState ? VISIBLE : GONE);
        addView(view);
        return this;
    }

    @Override
    final public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        // Log.e("TAG", "childCount -->>> " + getChildCount());
        if (getChildCount() > 4) {
            throw new IllegalStateException("StateLayout only can one content view.");
        }
        //确定内容布局
        if (mSparseArray.get(CONTENT) == null && (mSparseArray.indexOfValue(child)) < 0) {
            if (mState != CONTENT) {
                child.setVisibility(GONE);
            }
            mSparseArray.put(CONTENT, child);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getChildCount() < 4 && (DEFAULT_LOADING_LAYOUT_ID == NO_LAYOUT_ID ||
                DEFAULT_ERROR_LAYOUT_ID == NO_LAYOUT_ID || DEFAULT_EMPTY_LAYOUT_ID == NO_LAYOUT_ID)) {
            throw new NullPointerException("please at your application " +
                    "set StateLayout.Builder.newBuilder().build().");
        }
    }


    //////////////////////////////////////////////////////////

    /**
     * 获取当前状态
     *
     * @return
     */
    @ViewKey
    public int getState() {
        return mState;
    }

    /**
     * 获取当前状态视图
     *
     * @return
     */
    public View getStateView() {
        return mSparseArray.get(mState);
    }

    /**
     * 使用主线程显示
     *
     * @param key
     */
    private void showView(@IntRange(from = 0, to = 4) @ViewKey final int key) {
        //重复判断
        if (key == mState || mAniming) {
            if (mStateListener != null) {
                mStateListener.onCancel();
            }
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _showView(key);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    _showView(key);
                }
            });
        }
    }

    private void _showView(final int tag) {
        final View stateView = mSparseArray.get(mState);
        final View tagView = mSparseArray.get(tag);
        if (mStateListener != null) {
            mStateListener.onPreper();
        }
        if (mUseAnim && mViewAnims != null) {
            animTranslate(tag, stateView, tagView);
        } else {
            if (mStateListener != null) {
                mStateListener.onStart(mState, tag, false);
            }
            stateView.setVisibility(GONE);
            tagView.setVisibility(VISIBLE);
            mState = tag;
            if (mStateListener != null) {
                mStateListener.onComplete();
            }
        }
    }

    /**
     * 切换
     *
     * @param target
     * @param stateView
     * @param targetView
     */
    private void animTranslate(int target, View stateView, final View targetView) {
        Animator disappear = mViewAnims.providerDisappear(mState, stateView);
        Animator appear = mViewAnims.providerAppear(target, targetView);
        if (mAnimType == SEQUENCE) {
            sequentiallyAnim(target, stateView, targetView, disappear, appear);
        } else {
            togetherAnim(target, stateView, targetView, disappear, appear);
        }
    }

    /**
     * 同时动画
     *
     * @param target     目标状态
     * @param stateView  当前视图
     * @param targetView 目标视图
     * @param disappear  消失动画
     * @param appear     出现的动画
     */
    private void togetherAnim(final int target, final View stateView,
                              final View targetView, Animator disappear, Animator appear) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(disappear, appear);
        targetView.setVisibility(VISIBLE);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //stateView.setVisibility(GONE);
                //targetView.setVisibility(VISIBLE);
                mState = target;
                mAniming = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAniming = true;
            }
        });
        animatorSet.start();
    }

    /**
     * 顺序动画
     *
     * @param target     目标状态
     * @param stateView  当前视图
     * @param targetView 目标视图
     * @param disappear  消失动画
     * @param appear     出现的动画
     */
    private void sequentiallyAnim(final int target, final View stateView,
                                  final View targetView, Animator disappear, final Animator appear) {
//        // 一
//        disappear.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                super.onAnimationStart(animation);
//                if (mStateListener != null) {
//                    mStateListener.onStart(mState, target, true);
//                }
//                mAniming = true;
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                //stateView.setVisibility(GONE);
//                targetView.setVisibility(VISIBLE);
//                appear.start();
//            }
//        });
//        appear.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                mState = target;
//                mAniming = false;
//                if (mStateListener != null) {
//                    mStateListener.onComplete();
//                }
//            }
//        });
//        disappear.start();

//       二
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(disappear, appear);
        disappear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                targetView.setVisibility(VISIBLE);
            }
        });
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (mStateListener != null) {
                    mStateListener.onStart(mState, target, true);
                }
                mAniming = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mStateListener != null) {
                    mStateListener.onComplete();
                }
                mState = target;
                mAniming = false;
            }
        });
        animatorSet.start();
    }

    public View getView(@ViewKey int key) {
        return mSparseArray.get(key);
    }

    public <T extends View> T getView(@ViewKey int key, @IdRes int IdRes) {
        return (T) mSparseArray.get(key).findViewById(IdRes);
    }

    public StateLayout setOnClickEvent(@ViewKey int key, View.OnClickListener listener) {
        getView(key).setOnClickListener(listener);
        return this;
    }

    public StateLayout setOnClickEvent(@ViewKey int key, @IdRes int IdRes, View.OnClickListener listener) {
        getView(key, IdRes).setOnClickListener(listener);
        return this;
    }

    public StateLayout setLongClickEvent(@ViewKey int key, View.OnLongClickListener listener) {
        getView(key).setOnLongClickListener(listener);
        return this;
    }

    public StateLayout setLongClickEvent(@ViewKey int key, @IdRes int IdRes, View.OnLongClickListener listener) {
        getView(key, IdRes).setOnLongClickListener(listener);
        return this;
    }

    public StateLayout setOnTouchEvent(@ViewKey int key, View.OnTouchListener listener) {
        getView(key).setOnTouchListener(listener);
        return this;
    }

    public StateLayout setTouchEvent(@ViewKey int key, @IdRes int IdRes, View.OnTouchListener listener) {
        getView(key, IdRes).setOnTouchListener(listener);
        return this;
    }

    public StateLayout setUseAnim(boolean useAnim) {
        mUseAnim = useAnim;
        return this;
    }

    public boolean isUseAnim() {
        return mUseAnim;
    }

    public int getAnimType() {
        return mAnimType;
    }

    public StateLayout setAnimType(@IntRange(from = 0, to = 1) int mType) {
        this.mAnimType = type;
        return this;
    }

    public ViewAnims getViewAnims() {
        return mViewAnims;
    }

    public StateLayout setViewAnims(ViewAnims viewAnims) {
        mViewAnims = viewAnims;
        return this;
    }

    ////////////////////////////// 定制属性 //////////////////////////////

    public void showContent() {
        showView(CONTENT);
    }

    public void showLoading() {
        showView(LOADING);
    }

    public void showError() {
        showView(ERROR);
    }

    public void showEmpty() {
        showView(EMPTY);
    }

    public View getContentView() {
        return getView(CONTENT);
    }

    public View getContentView(@IdRes int IdRes) {
        return getView(CONTENT, IdRes);
    }

    public View getLoadingView() {
        return getView(LOADING);
    }

    public View getLoadingView(@IdRes int IdRes) {
        return getView(LOADING, IdRes);
    }

    public View getErrorView() {
        return getView(ERROR);
    }

    public View getErrorView(@IdRes int IdRes) {
        return getView(ERROR, IdRes);
    }

    public View getEmptyView() {
        return getView(EMPTY);
    }

    public View getEmptyView(@IdRes int IdRes) {
        return getView(EMPTY, IdRes);
    }


    /**
     * 预置属性
     */
    public static class Builder {

        private static final Builder mInstance = new Builder();

        private Builder() {
        }

        private int default_loading_Layout;
        private int default_empty_Layout;
        private int default_error_Layout;
        private boolean default_useAnim;
        private int default_anim_type;
        private ViewAnims default_viewanims;

        public static Builder newBuilder() {
            return mInstance;
        }

        public Builder setLoadingId(@LayoutRes int layoutRes) {
            this.default_loading_Layout = layoutRes;
            return mInstance;
        }

        public Builder setEmptyId(@LayoutRes int layoutRes) {
            this.default_empty_Layout = layoutRes;
            return mInstance;
        }

        public Builder setErrorId(@LayoutRes int layoutRes) {
            this.default_error_Layout = layoutRes;
            return mInstance;
        }

        public Builder setType(@AnimType int type) {
            this.default_anim_type = type;
            return this;
        }

        public Builder setUseAnim(boolean useAnim) {
            this.default_useAnim = useAnim;
            return this;
        }

        public Builder setViewAnims(ViewAnims viewAnims) {
            this.default_viewanims = viewAnims;
            return this;
        }

        public void build() {
            if (default_loading_Layout == 0) {
                throw new NullPointerException("please set default_loading_Layout.");
            }
            if (default_empty_Layout == 0) {
                throw new NullPointerException("please set default_empty_Layout.");
            }
            if (default_error_Layout == 0) {
                throw new NullPointerException("please set default_error_Layout.");
            }
            DEFAULT_LOADING_LAYOUT_ID = default_loading_Layout;
            DEFAULT_EMPTY_LAYOUT_ID = default_empty_Layout;
            DEFAULT_ERROR_LAYOUT_ID = default_error_Layout;
            DEFAULT_USE_ANIM = default_useAnim;
            if (default_anim_type != 0) {
                DEFAULT_ANIM_TYPE = default_anim_type;
            }
            if (default_viewanims != null) {
                DEFAULT_VIEW_ANIMAS = default_viewanims;
            } else {
                DEFAULT_VIEW_ANIMAS = new DefaultViewAnims();
            }
        }
    }


}
