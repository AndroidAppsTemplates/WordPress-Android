package org.wordpress.android.viewmodel.pages

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.wordpress.android.R
import org.wordpress.android.R.string
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.page.PageModel
import org.wordpress.android.fluxc.model.page.PageStatus.PUBLISHED
import org.wordpress.android.fluxc.store.PageStore
import org.wordpress.android.ui.pages.PageItem
import org.wordpress.android.ui.pages.PageItem.Divider
import org.wordpress.android.ui.pages.PageItem.Empty
import org.wordpress.android.ui.pages.PageItem.ParentPage
import org.wordpress.android.ui.pages.PageItem.Type.PARENT
import org.wordpress.android.ui.pages.PageItem.Type.TOP_LEVEL_PARENT
import org.wordpress.android.viewmodel.ResourceProvider
import javax.inject.Inject

class PageParentViewModel
@Inject constructor(private val pageStore: PageStore, private val resourceProvider: ResourceProvider) : ViewModel() {
    private val _pages: MutableLiveData<List<PageItem>> = MutableLiveData()
    val pages: LiveData<List<PageItem>> = _pages

    private lateinit var _currentParent: ParentPage
    val currentParent: ParentPage
        get() = _currentParent

    private val _isSaveButtonVisible = MutableLiveData<Boolean>()
    val isSaveButtonVisible: LiveData<Boolean> = _isSaveButtonVisible

    private var isStarted: Boolean = false
    private lateinit var site: SiteModel
    private var page: PageModel? = null

    fun start(site: SiteModel, pageId: Long) {
        this.site = site

        if (!isStarted) {
            _pages.postValue(listOf(Empty(string.empty_list_default)))
            isStarted = true

            loadPages(pageId)

            _isSaveButtonVisible.postValue(false)
        }
    }

    private fun loadPages(pageId: Long) = launch(CommonPool) {
        page = pageStore.getPageByRemoteId(pageId, site)

        val parents = mutableListOf<PageItem>(
                ParentPage(0, resourceProvider.getString(R.string.top_level),
                        page?.parent == null,
                        TOP_LEVEL_PARENT)
        )

        val choices = pageStore.getPagesFromDb(site).filter { it.remoteId != pageId && it.status == PUBLISHED }
        val parentChoices = choices.filter { isNotChild(it, choices) }
        if (parentChoices.isNotEmpty()) {
            parents.add(Divider(resourceProvider.getString(R.string.pages)))
            parents.addAll(parentChoices.map {
                ParentPage(it.remoteId, it.title, page?.parent?.remoteId == it.remoteId, PARENT)
            })
        }

        _currentParent = parents.firstOrNull { it is ParentPage && it.isSelected } as? ParentPage
                ?: parents.first() as ParentPage

        _pages.postValue(parents)
    }

    fun onParentSelected(page: ParentPage) {
        _currentParent.isSelected = false
        _currentParent = page
        _currentParent.isSelected = true

        _isSaveButtonVisible.postValue(true)
    }

    private fun isNotChild(choice: PageModel, choices: List<PageModel>): Boolean {
        return !getChildren(page!!, choices).contains(choice)
    }

    private fun getChildren(page: PageModel, pages: List<PageModel>): List<PageModel> {
        val children = pages.filter { it.parent?.remoteId == page.remoteId }
        val grandchildren = mutableListOf<PageModel>()
        children.forEach {
            grandchildren += getChildren(it, pages)
        }
        return children + grandchildren
    }
}
