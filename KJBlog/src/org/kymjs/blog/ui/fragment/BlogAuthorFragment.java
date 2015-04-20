package org.kymjs.blog.ui.fragment;

import java.util.List;

import org.kymjs.blog.R;
import org.kymjs.blog.adapter.BlogAuthorAdapter;
import org.kymjs.blog.domain.BlogAuthor;
import org.kymjs.blog.domain.SimpleBackPage;
import org.kymjs.blog.ui.SimpleBackActivity;
import org.kymjs.blog.ui.widget.listview.FooterLoadingLayout;
import org.kymjs.blog.ui.widget.listview.PullToRefreshBase;
import org.kymjs.blog.ui.widget.listview.PullToRefreshBase.OnRefreshListener;
import org.kymjs.blog.ui.widget.listview.PullToRefreshList;
import org.kymjs.blog.utils.Parser;
import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpConfig;
import org.kymjs.kjframe.ui.BindView;
import org.kymjs.kjframe.utils.KJLoger;
import org.kymjs.kjframe.utils.StringUtils;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BlogAuthorFragment extends TitleBarFragment {

    public static final String TAG = BlogAuthorFragment.class.getSimpleName();

    public static final String AUTHOR_NAME_KEY = "author_name_key";

    @BindView(id = R.id.listview)
    private PullToRefreshList mRefreshLayout;
    private ListView mListView;

    private BlogAuthorAdapter adapter;

    private KJHttp kjh;

    private final String OSCBLOG_HOST = "http://www.kymjs.com/api/json_blog_author";
    private String cache;

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container,
            Bundle bundle) {
        View rootView = inflater.inflate(R.layout.frag_pull_refresh_listview,
                container, false);
        return rootView;
    }

    @Override
    protected void setActionBarRes(ActionBarRes actionBarRes) {
        super.setActionBarRes(actionBarRes);
        actionBarRes.title = getString(R.string.str_follow);
        actionBarRes.backImageId = R.drawable.titlebar_back;
    }

    @Override
    protected void initData() {
        super.initData();
        HttpConfig config = new HttpConfig();
        config.cacheTime = 300;
        config.useDelayCache = true;
        kjh = KJHttp.create(config);
    }

    @Override
    protected void initWidget(View parentView) {
        super.initWidget(parentView);
        mListView = mRefreshLayout.getRefreshView();
        mListView.setDivider(new ColorDrawable(android.R.color.transparent));
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putInt(OSCBlogListFragment.BLOGLIST_KEY,
                        ((BlogAuthor) adapter.getItem(position)).getId());
                bundle.putString(AUTHOR_NAME_KEY,
                        ((BlogAuthor) adapter.getItem(position)).getName());
                SimpleBackActivity.postShowWith(outsideAty,
                        SimpleBackPage.OSC_BLOG_LIST, bundle);
            }
        });
        mRefreshLayout.setPullLoadEnabled(true);
        ((FooterLoadingLayout) mRefreshLayout.getFooterLoadingLayout())
                .setNoMoreDataText("学习不可贪多哦");
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                refresh();
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                mRefreshLayout.setHasMoreData(false);
            }
        });

        fillUI();
    }

    private void fillUI() {
        cache = kjh.getCache(OSCBLOG_HOST, null);
        if (!StringUtils.isEmpty(cache)) {
            List<BlogAuthor> datas = Parser.getBlogAuthor(cache);
            if (adapter == null) {
                adapter = new BlogAuthorAdapter(outsideAty, datas);
                mListView.setAdapter(adapter);
            } else {
                adapter.refresh(datas);
            }
        }
        refresh();
    }

    private void refresh() {
        kjh.get(OSCBLOG_HOST, new HttpCallBack() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                KJLoger.debug(TAG + "网络请求：" + t);
                mRefreshLayout.onPullDownRefreshComplete();
                if (t != null && !t.equals(cache)) {
                    List<BlogAuthor> datas = Parser.getBlogAuthor(t);
                    if (adapter == null) {
                        adapter = new BlogAuthorAdapter(outsideAty, datas);
                        mListView.setAdapter(adapter);
                    } else {
                        adapter.refresh(datas);
                    }
                }
            }
        });
    }

    @Override
    public void onBackClick() {
        super.onBackClick();
        outsideAty.finish();
    }
}