package models

case class SummaryEvent(
    connectionId: String,
    chatbotId: Int,
    campaignId: Int,
    interactionId: Int,
    theTime: java.sql.Timestamp,
    secondsToActive: Int,
    secondsTotalActive: Int,
    chatbotActions: Int,
    clickedLink: Int,
    timeGoalMet: Int,
    timeGoalSeconds: Int,
    reachedEnd: Int,
    reachedGoal: Int,
    reachedInput: Int,
    flowsCompleted: Int
)
