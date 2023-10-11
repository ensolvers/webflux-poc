# Chat Conversations

Core supports an out-of-the-box model for implemeting chat conversations. The model is composed by 3 classes:
- [`ChatChannel`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/model/ChatChannel.java), which represents a "group" in which two or more members can interchange messages - something analogous to a Slack channel
- [`ChatChannelMembership`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/model/ChatChannelMembership.java), which represents the membership of a `User` to a particular `ChatChannel`, including some extra information like the particular role, last time the user accessed to the channel, etc.
- [`ChatChannelMessage`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/model/ChatMessage.java), which represents a concrete message sent ot a ChatChannel for a particular User

Core supports managing channels, memberships to those channels and messages sent to them via the following services
- [`ChatChannelService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/ChatChannelService.java) 
- [`ChatChannelMembershipService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/ChatChannelMembershipService.java)
- [`ChatMessageService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/ChatMessageService.java)
