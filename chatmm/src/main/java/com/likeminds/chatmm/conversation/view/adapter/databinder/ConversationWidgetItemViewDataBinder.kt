package com.likeminds.chatmm.conversation.view.adapter.databinder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.likeminds.chatmm.LMAnalytics
import com.likeminds.chatmm.branding.model.LMBranding
import com.likeminds.chatmm.chatroom.detail.util.ChatroomConversationItemViewDataBinderUtil
import com.likeminds.chatmm.chatroom.detail.view.adapter.ChatroomDetailAdapterListener
import com.likeminds.chatmm.conversation.model.ConversationViewData
import com.likeminds.chatmm.databinding.ItemConversationCustomWidgetBinding
import com.likeminds.chatmm.member.util.UserPreferences
import com.likeminds.chatmm.reactions.util.ReactionUtil
import com.likeminds.chatmm.reactions.util.ReactionsPreferences
import com.likeminds.chatmm.utils.ViewUtils.hide
import com.likeminds.chatmm.utils.ViewUtils.show
import com.likeminds.chatmm.utils.customview.ViewDataBinder
import com.likeminds.chatmm.utils.model.ITEM_CONVERSATION_CUSTOM_WIDGET
import org.json.JSONObject

class ConversationWidgetItemViewDataBinder(
    private val userPreferences: UserPreferences,
    private val reactionsPreferences: ReactionsPreferences,
    private val adapterListener: ChatroomDetailAdapterListener
) : ViewDataBinder<ItemConversationCustomWidgetBinding, ConversationViewData>() {
    override val viewType: Int
        get() = ITEM_CONVERSATION_CUSTOM_WIDGET

    override fun createBinder(parent: ViewGroup): ItemConversationCustomWidgetBinding {
        val binding = ItemConversationCustomWidgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return binding
    }

    override fun bindData(
        binding: ItemConversationCustomWidgetBinding,
        data: ConversationViewData,
        position: Int
    ) {
        binding.apply {
            buttonColor = LMBranding.getButtonsColor()
            viewReply.buttonColor = LMBranding.getButtonsColor()
            conversation = data

            val context = root.context

            //Custom Widget Data
            val metadata = JSONObject(data.widgetViewData?.metadata.toString())

            //get whether chatroom is dm or not
            val isDMChatroom = if (metadata.has("is_dm")) {
                metadata.get("is_dm") as Boolean
            } else {
                false
            }

            //get transaction id
            val transactionId = if (metadata.has("transaction_id")) {
                metadata.get("transaction_id") as? String
            } else {
                null
            }

            // get widget type to be displayed
            val widgetType = if (metadata.has("widget_type")) {
                metadata.get("widget_type") as? String
            } else {
                null
            }

            if (!transactionId.isNullOrEmpty()) {
                clWidget.show()
                val timeStamp = if (metadata.has("timestamp")) {
                    metadata.get("timestamp") as? Long
                } else {
                    null
                }

                tvKey1.text = if (timeStamp != null) {
                    "$timeStamp"
                } else {
                    "null timestamp"
                }
            } else {
                clWidget.hide()
            }

            ChatroomConversationItemViewDataBinderUtil.initConversationBubbleView(
                clConversationRoot,
                clConversationBubble,
                memberImage,
                tvConversationMemberName,
                tvCustomTitle,
                tvCustomTitleDot,
                data.memberViewData,
                userPreferences.getUUID(),
                adapterListener,
                position,
                conversationViewData = data,
                imageViewStatus = ivConversationStatus,
                imageViewFailed = ivConversationFailed
            )

            if (data.deletedBy != null) {
                ChatroomConversationItemViewDataBinderUtil.initConversationBubbleDeletedTextView(
                    tvConversation,
                    tvDeleteMessage,
                    userPreferences.getUUID(),
                    conversationViewData = data
                )
                ivAddReaction.hide()
            } else {
                ChatroomConversationItemViewDataBinderUtil.initConversationBubbleTextView(
                    tvConversation,
                    data.answer,
                    position,
                    createdAt = data.createdAt,
                    conversation = data,
                    adapterListener = adapterListener,
                    tvDeleteMessage = tvDeleteMessage
                )

                val uuid = data.memberViewData.sdkClientInfo.uuid
                if (uuid == userPreferences.getUUID()) {
                    ivAddReaction.hide()
                } else {
                    if (data.answer.length > ConversationItemViewDataBinder.ADD_REACTION_CHARACTER_CHECK || !data.reactions
                            .isNullOrEmpty()
                    ) {
                        ivAddReaction.show()
                    } else {
                        ivAddReaction.hide()
                    }
                }
            }
            ChatroomConversationItemViewDataBinderUtil.initTimeAndStatus(
                tvTime,
                userPreferences.getUUID(),
                data.createdAt,
                imageViewStatus = ivConversationStatus,
                conversation = data
            )

            ChatroomConversationItemViewDataBinderUtil.initReplyView(
                viewReply,
                userPreferences.getUUID(),
                data.replyConversation,
                data.replyChatroomId,
                adapterListener,
                itemPosition = position,
                conversation = data
            )

            ChatroomConversationItemViewDataBinderUtil.initSelectionAnimation(
                binding.viewSelectionAnimation, position, adapterListener
            )

            ChatroomConversationItemViewDataBinderUtil.initReportView(
                ivReport,
                userPreferences.getUUID(),
                adapterListener,
                conversationViewData = data
            )

            val viewList = listOf(
                root,
                memberImage,
                tvConversation,
                viewReply.root,
                ivReport
            )
            isSelected =
                ChatroomConversationItemViewDataBinderUtil.initConversationSelection(
                    root,
                    viewList,
                    data,
                    position,
                    adapterListener
                )

            val messageReactionsGridViewData = ReactionUtil.getReactionsGrid(data)

            ChatroomConversationItemViewDataBinderUtil.initMessageReactionGridView(
                messageReactionsGridViewData,
                clConversationRoot,
                binding.clConversationBubble,
                binding.messageReactionsGridLayout,
                userPreferences.getUUID(),
                adapterListener,
                data
            )

            val isReactionHintShown =
                ChatroomConversationItemViewDataBinderUtil.isReactionHintViewShown(
                    data.isLastItem,
                    reactionsPreferences.getHasUserReactedOnce(),
                    reactionsPreferences.getNoOfTimesHintShown(),
                    reactionsPreferences.getTotalNoOfHintsAllowed(),
                    binding.tvDoubleTap,
                    data.memberViewData,
                    userPreferences.getUUID(),
                    clConversationRoot,
                    clConversationBubble
                )
            if (isReactionHintShown) {
                adapterListener.reactionHintShown()
            }

            ivAddReaction.setOnClickListener {
                adapterListener.onLongPressConversation(
                    data,
                    position,
                    LMAnalytics.Source.MESSAGE_REACTIONS_FROM_REACTION_BUTTON
                )
            }
        }
    }
}