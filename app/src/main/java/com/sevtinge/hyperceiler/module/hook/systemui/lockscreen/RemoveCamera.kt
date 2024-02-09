/*
  * This file is part of HyperCeiler.

  * HyperCeiler is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.

  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <https://www.gnu.org/licenses/>.

  * Copyright (C) 2023-2024 HyperCeiler Contributions
*/
package com.sevtinge.hyperceiler.module.hook.systemui.lockscreen

import android.view.View
import android.widget.LinearLayout
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import com.sevtinge.hyperceiler.module.base.BaseHook
import com.sevtinge.hyperceiler.utils.devicesdk.isAndroidVersion
import com.sevtinge.hyperceiler.utils.devicesdk.isMiuiVersion
import com.sevtinge.hyperceiler.utils.devicesdk.isMoreHyperOSVersion
import com.sevtinge.hyperceiler.utils.getObjectField

object RemoveCamera : BaseHook() {
    private val oldClass by lazy {
        loadClass("com.android.systemui.statusbar.phone.KeyguardBottomAreaView")
    }
    private val newClass by lazy {
        loadClass("com.android.keyguard.injector.KeyguardBottomAreaInjector")
    }

    override fun init() {
        // 屏蔽右下角组件显示
        if (isMoreHyperOSVersion(1f)) {
             newClass.methodFinder().filter {
                 name in setOf(
                     "updateRightAffordanceViewLayoutVisibility",
                     "startButtonLayoutAnimate"
                 )
             }.toList().createHooks {
                 after {
                     val right = it.thisObject.getObjectField("mRightAffordanceViewLayout") as LinearLayout
                     right.visibility = View.GONE
                 }
             }
        } else if (isMiuiVersion(14f) && isAndroidVersion(34)) {
            newClass.methodFinder().first() {
                name == "onFinishInflate"
            }.createHook {
                after {
                    val right = it.thisObject.getObjectField("mRightAffordanceViewLayout") as LinearLayout
                    right.visibility = View.GONE
                }
            }
        } else {
            oldClass.methodFinder().first {
                name == "onFinishInflate"
            }.createHook {
                after {
                    (it.thisObject.getObjectField("mRightAffordanceViewLayout") as LinearLayout).visibility =
                        View.GONE
                }
            }
        }

        // 屏蔽滑动撞墙动画
        loadClass("com.android.keyguard.KeyguardMoveRightController").methodFinder().first {
            name == "onTouchMove" && parameterCount == 2
        }.createHook {
            before {
                it.result = false
            }
        }
        loadClass("com.android.keyguard.KeyguardMoveRightController").methodFinder().first {
            name == "reset"
        }.createHook {
            before {
                it.result = null
            }
        }
    }
}
