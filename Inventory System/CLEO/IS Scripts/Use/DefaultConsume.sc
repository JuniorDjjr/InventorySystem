// by Junior_Djjr - MixMods.com.br
// You need: https://forum.mixmods.com.br/f141-gta3script-cleo/t5206-como-criar-scripts-com-cleo
SCRIPT_START
{
    // In: Char handle and pointer to stored item
    LVAR_INT hChar pStoredItem
    // In: Your values configured in .ini file, MUST RECEIVE AS FLOAT, if you need INT, just convert it (i =# f).
    LVAR_FLOAT fHealth fHungry fCalories fAnimID fDrunkness

    // Your variables definition
    LVAR_INT i iAnimID bOk bConsumed iAudio
    LVAR_FLOAT f fMaxHealth fFinishAnimTime fAnimProgress
    LVAR_TEXT_LABEL16 tIFP tAnim

    // Constants - Prefer using them for read only

    // Item stored struct params IDs
    CONST_INT STORED_ITEM_ID 0
    CONST_INT STORED_ITEM_DATA 1
    CONST_INT STORED_COUNT 2 // prefer using ITEM_FLAG_DECREASE_COUNT
    CONST_INT STORED_FLAGS 3
    
    // Item flags
    CONST_INT ITEM_FLAG_USING 0
    CONST_INT ITEM_FLAG_DECREASE_COUNT 1
    CONST_INT ITEM_FLAG_HIDE 2

    // Item data struct param IDs
    CONST_INT DATA_ITEM_ID 0
    CONST_INT DATA_MODEL_ID 1
    CONST_INT DATA_BONE 2
    CONST_INT DATA_STACK 3

    // End script if not called by script root, or wrong char arg
    IF hChar = 0
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    // -----------------------------
    // Your code called when using the item

    GET_PED_TYPE hChar i
    IF i < 2 // is player
        SET_PLAYER_CYCLE_WEAPON_BUTTON i OFF
    ENDIF

    iAnimID =# fAnimID

    SWITCH iAnimID
        CASE 0
            tIFP = VENDING
            tAnim = VEND_Eat_P
            fFinishAnimTime = 1.0
            iAudio = SOUND_RESTAURANT_CJ_EAT
            BREAK
        CASE 1
            tIFP = VENDING
            tAnim = vend_eat1_P
            fFinishAnimTime = 0.7
            iAudio = SOUND_RESTAURANT_CJ_EAT
            BREAK
        CASE 2
            tIFP = FOOD
            tAnim = EAT_Burger
            fFinishAnimTime = 0.6
            iAudio = SOUND_RESTAURANT_CJ_EAT
            BREAK
        CASE 3
            tIFP = FOOD
            tAnim = EAT_Chicken
            fFinishAnimTime = 0.7
            iAudio = SOUND_RESTAURANT_CJ_EAT
            BREAK
        CASE 4
            tIFP = FOOD
            tAnim = EAT_Pizza
            fFinishAnimTime = 0.9
            iAudio = SOUND_RESTAURANT_CJ_EAT
            BREAK
        CASE 5
            tIFP = VENDING
            tAnim = VEND_Drink_P
            fFinishAnimTime = 1.0
            iAudio = -1
            BREAK
    ENDSWITCH

    REQUEST_ANIMATION $tIFP
    LOAD_ALL_MODELS_NOW

    TASK_PLAY_ANIM_SECONDARY hChar $tAnim $tIFP 3.0 FALSE FALSE FALSE FALSE -1

    IF NOT iAudio = -1
        REPORT_MISSION_AUDIO_EVENT_AT_CHAR hChar SOUND_RESTAURANT_CJ_EAT
    ENDIF

    // Wait animation
    timera = 0
    fAnimProgress = 0.0
    WHILE fAnimProgress < 1.0
        WAIT 0
        IF NOT DOES_CHAR_EXIST hChar
        OR NOT IS_CHAR_PLAYING_ANIM hChar $tAnim
        OR timera > 7000 //avoid soft lock
            BREAK
        ENDIF
        GET_CHAR_ANIM_CURRENT_TIME hChar $tAnim fAnimProgress
        IF fAnimProgress >= fFinishAnimTime
        AND bConsumed = FALSE
            // Update char health
            GET_CHAR_HEALTH hChar i
            f =# i
            f += fHealth
            GET_CHAR_MAX_HEALTH hChar fMaxHealth
            IF f > fMaxHealth
                f = fMaxHealth
            ENDIF
            i =# f
            SET_CHAR_HEALTH hChar i
            bConsumed = TRUE
            GOSUB HideObject
        ENDIF
    ENDWHILE

    GET_PED_TYPE hChar i
    IF i < 2 // is player
        SET_PLAYER_CYCLE_WEAPON_BUTTON i ON
    ENDIF

    WAIT 300

    IF bConsumed = TRUE
        GOSUB DecreaseCount // it will decrease 1 item count next frame
    ENDIF
    GOSUB StopUsing

    REMOVE_ANIMATION $tIFP
    TERMINATE_THIS_CUSTOM_SCRIPT

    //-------------------------------------------

    HideObject:
    READ_STRUCT_PARAM pStoredItem STORED_FLAGS i
    SET_LOCAL_VAR_BIT_CONST i ITEM_FLAG_HIDE
    WRITE_STRUCT_PARAM pStoredItem STORED_FLAGS i
    RETURN

    DecreaseCount:
    READ_STRUCT_PARAM pStoredItem STORED_FLAGS i
    SET_LOCAL_VAR_BIT_CONST i ITEM_FLAG_DECREASE_COUNT
    WRITE_STRUCT_PARAM pStoredItem STORED_FLAGS i
    RETURN

    StopUsing:
    READ_STRUCT_PARAM pStoredItem STORED_FLAGS i
    CLEAR_LOCAL_VAR_BIT_CONST i ITEM_FLAG_USING
    WRITE_STRUCT_PARAM pStoredItem STORED_FLAGS i
    RETURN
}
SCRIPT_END