// 文件路径: app/src/main/java/com/zhiyun/agentrobot/ui/guide/GuideData.kt

package com.zhiyun.agentrobot.ui.guide // 确认包名与文件路径一致

import androidx.annotation.DrawableRes
import com.zhiyun.agentrobot.R // 导入R文件以引用drawable资源

/**
 * 为导览界面的“精装修”阶段创建的临时UI数据模型。
 * 它将按钮名称、按钮小图标、轮播图大图三者关联起来。
 * 这个数据是导览页专属的，因此定义在此文件中。
 */
data class GuidePageUiItem(
    val name: String,
    @DrawableRes val iconResId: Int, // 使用 @DrawableRes 注解确保传入的是Drawable资源ID
    @DrawableRes val bigImageResId: Int
)

/**
 * 创建一个临时的、静态的UI数据源列表。
 * 它为我们的导览页提供了在没有真实数据时用于预览和调试的“假数据”。
 * 列表的顺序，就是UI上从上到下的按钮顺序，和轮播图从0开始的页面顺序。
 */
val guideUiItems = listOf(
    GuidePageUiItem(
        name = "公司概览",
        iconResId = R.drawable.ic_company_overview,
        bigImageResId = R.drawable.image_company_view_big
    ),
    GuidePageUiItem(
        name = "智芸数据",
        iconResId = R.drawable.ic_zhiyun_data_company,
        bigImageResId = R.drawable.image_health_brain_big // 注意：这里我们之前讨论时对应的是康养大脑图
    ),
    GuidePageUiItem(
        name = "表情包合影",
        iconResId = R.drawable.ic_meme_group_photo,
        bigImageResId = R.drawable.image_virtual_human_big
    )
)
