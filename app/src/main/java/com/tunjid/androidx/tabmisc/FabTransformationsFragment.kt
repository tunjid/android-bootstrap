package com.tunjid.androidx.tabmisc

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.postDelayed
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.doOnEveryEvent
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.core.graphics.drawable.withTint
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.material.animator.FabExtensionAnimator
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.uidrivers.SpeedDialClickListener
import com.tunjid.androidx.uidrivers.callback
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.updatePartial
import com.tunjid.androidx.view.util.withOneShotEndListener

class FabTransformationsFragment : Fragment(R.layout.fragment_fab_transformations) {

    private var isTopLevel by fragmentArgs<Boolean>()

    private val color
        get() = if (requireContext().isDarkTheme) Color.BLACK else Color.WHITE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context
        val speedDialItems = createSpeedDialItems()

        val demoFab = view.findViewById<MaterialButton>(R.id.expandable_fab)
        val extender = FabExtensionAnimator(demoFab).apply {
            speedDialItems[1].run { updateGlyphs(first, second) }
        }

        val speedDialClickListener = SpeedDialClickListener(
            tint = context.themeColorAt(R.attr.colorAccent),
            items = speedDialItems,
            runGuard = this@FabTransformationsFragment::fabExtensionGuard,
            dismissListener = {
                when (it) {
                    null -> Unit
                    0 -> uiState = uiState.copy(fabExtended = true)
                    else -> speedDialItems[it].run {
                        extender.updateGlyphs(first, second)
                    }
                }
            }
        )

        if (isTopLevel) uiState = uiState.copy(
            toolbarTitle = this::class.java.routeName,
            toolbarOverlaps = false,
            toolbarShows = true,
            toolbarMenuRes = 0,
            fabShows = true,
            fabExtended = true,
            fabText = getString(R.string.speed_dial),
            fabIcon = R.drawable.ic_unfold_more_24dp,
            showsBottomNav = false,
            insetFlags = InsetFlags.ALL,
            lightStatusBar = !context.isDarkTheme,
            navBarColor = context.themeColorAt(R.attr.nav_bar_color),
            fabClickListener = viewLifecycleOwner.callback(speedDialClickListener)
        )
        else viewLifecycleOwner.lifecycle.doOnEveryEvent(Lifecycle.Event.ON_RESUME) {
            ::uiState.updatePartial {
                copy(
                    fabShows = true,
                    fabText = getString(R.string.speed_dial),
                    fabIcon = R.drawable.ic_unfold_more_24dp,
                    fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                    fabClickListener = viewLifecycleOwner.callback(speedDialClickListener)
                )
            }
        }

        demoFab.setOnClickListener { extender.isExtended = !extender.isExtended }

        view.findViewById<ChipGroup>(R.id.spring_stiffness).check(extender, SpringForce::setStiffness) {
            when (it) {
                R.id.stiffness_very_low -> SpringForce.STIFFNESS_VERY_LOW
                R.id.stiffness_low -> SpringForce.STIFFNESS_LOW
                R.id.stiffness_medium -> SpringForce.STIFFNESS_MEDIUM
                R.id.stiffness_high -> SpringForce.STIFFNESS_HIGH
                else -> SpringForce.STIFFNESS_VERY_LOW
            }
        }

        view.findViewById<ChipGroup>(R.id.spring_damping).check(extender, SpringForce::setDampingRatio) {
            when (it) {
                R.id.damping_none -> SpringForce.DAMPING_RATIO_NO_BOUNCY
                R.id.damping_low -> SpringForce.DAMPING_RATIO_LOW_BOUNCY
                R.id.damping_medium -> SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                R.id.damping_high -> SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                else -> SpringForce.DAMPING_RATIO_HIGH_BOUNCY
            }
        }

        view.postDelayed(2000) { if (isResumed) uiState = uiState.copy(fabExtended = false) }
    }

    private fun fabExtensionGuard(view: View): Boolean {
        if (!uiState.fabExtended) return true
        uiState = uiState.copy(
            fabExtended = false,
            fabTransitionOptions = viewLifecycleOwner.callback { it.speedDialRecall(view) }
        )
        return false
    }

    private fun SpringAnimation.speedDialRecall(view: View) = withOneShotEndListener {
        if (uiState.fabExtended) return@withOneShotEndListener
        uiState.fabClickListener.invoke(view)
    }

    private fun createSpeedDialItems() = requireActivity().run {
        listOf(
            getString(R.string.expand_fab).color(color) to drawableAt(R.drawable.ic_expand_24dp)
                ?.withTint(color)!!,
            getString(R.string.option_1).color(color) to drawableAt(R.drawable.ic_numeric_1_outline_24dp)
                ?.withTint(color)!!,
            getString(R.string.option_2).color(color) to drawableAt(R.drawable.ic_numeric_2_outline_24dp)
                ?.withTint(color)!!,
            getString(R.string.option_3).color(color) to drawableAt(R.drawable.ic_numeric_3_outline_24dp)
                ?.withTint(color)!!,
            getString(R.string.option_4).color(color) to drawableAt(R.drawable.ic_numeric_4_outline_24dp)
                ?.withTint(color)!!
        )
    }

    private fun ChipGroup.check(e: FabExtensionAnimator, springForce: SpringForce.(Float) -> SpringForce, paramMap: (Int) -> Float) {
        setOnCheckedChangeListener { _, checkedId -> e.configureSpring { springForce(spring, paramMap(checkedId)) } }
        e.configureSpring { springForce(spring, paramMap(checkedChipId)) }
    }

    companion object {
        fun newInstance(isTopLevel: Boolean): FabTransformationsFragment = FabTransformationsFragment().apply { this.isTopLevel = isTopLevel }
    }
}
