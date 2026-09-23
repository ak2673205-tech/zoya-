package com.example.automation

import kotlinx.coroutines.delay

class TaskExecutor(private val actionExecutor: ActionExecutor) {

    suspend fun executeSequence(steps: List<ActionStep>): ActionResult {
        for ((index, step) in steps.withIndex()) {
            val res = when (step) {
                is ActionStep.LaunchApp -> actionExecutor.executeLaunchApp(step.appName)
                is ActionStep.Delay -> {
                    delay(step.millis)
                    ActionResult(true, "Delayed ${step.millis}ms")
                }
                is ActionStep.Click -> actionExecutor.executeAccessibilityClick(step.target)
                is ActionStep.TypeText -> actionExecutor.executeAccessibilityType(step.text)
                is ActionStep.Scroll -> actionExecutor.executeAccessibilityScroll(step.down)
                is ActionStep.Back -> actionExecutor.executeBack()
                is ActionStep.Home -> actionExecutor.executeHome()
                is ActionStep.SearchWeb -> actionExecutor.executeWebSearch(step.query)
            }

            if (!res.success && step !is ActionStep.Delay) {
                return ActionResult(
                    success = false,
                    message = "Step ${index + 1} par rukawat aayi: ${res.message}"
                )
            }
        }

        return ActionResult(
            success = true,
            message = "Task successfully complete ho gaya."
        )
    }
}
