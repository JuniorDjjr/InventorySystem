SCRIPT_START
{
    // In: Char handle and pointer to stored item
    LVAR_INT hChar pStoredItem
    // In: Your values configured in .ini file, MUST RECEIVE AS FLOAT, if you need INT, just convert it (i =# f).
    LVAR_FLOAT fHealth fHungryMult fCalories fAnimID fThirst fStaminaTime

    // External var, must keep this index
    LVAR_INT iStaminaTimer
    
    // Your variables definition
    LVAR_INT i pScript pRootScript lItemsNames iPlayerId iSaveSlot iDefaultInfiniteRun iDefaultFastReload
    LVAR_TEXT_LABEL tItemName

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

    // Vars from root script that can be accessed from other scripts
    CONST_INT VAR_INVENTORY_SPACES 1 // pointer to stored items array
    CONST_INT VAR_ITEMS_NAMES 2 // items name list (each name is unique, item ID is the list index)
    CONST_INT VAR_ITEMS_DATA 3 // items data list
    CONST_INT VAR_ITEMS_SCRIPTS 4 // items scripts, values and icons list
    CONST_INT VAR_SELECTED_STORED_ITEM 5 // current stored item
    CONST_INT VAR_SELECTED_SLOT 6 // current slot
    CONST_INT VAR_USING_STORED_ITEM 7 // current stored item in use, 0 if not using item
    CONST_INT VAR_SELECTING_USE 8 // boolean is selecting use (you can use it to show some screen instruction etc)

    // End script if not called by script root, or wrong char arg
    IF hChar = 0
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    STREAM_CUSTOM_SCRIPT "IS Scripts\Use\DefaultConsume.cs" hChar pStoredItem fHealth fHungryMult fCalories fAnimID fThirst 0.0

    // Wait until item is consumed or not
    WHILE TRUE
        WAIT 0
        READ_STRUCT_PARAM pStoredItem STORED_FLAGS (i)
        IF NOT IS_LOCAL_VAR_BIT_SET_CONST i ITEM_FLAG_USING
            IF IS_LOCAL_VAR_BIT_SET_CONST i ITEM_FLAG_DECREASE_COUNT // item used
                BREAK
            ELSE
                TERMINATE_THIS_CUSTOM_SCRIPT
            ENDIF
        ENDIF
    ENDWHILE

    // Item consumed, set stamina boost

    fStaminaTime += 1.0
    fStaminaTime *= 10.0

    GET_PED_TYPE hChar iPlayerId
    IF iPlayerId > 0 //not player 1
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    // If script already exist, update stamina time
    GET_SCRIPT_STRUCT_NAMED ISSTMBO (pScript)
    IF pScript > 0x0
        //PRINT_STRING_NOW "already exist" 5000
        i =# fStaminaTime
        GET_SCRIPT_VAR pScript 8 (iStaminaTimer)
        iStaminaTimer += i
        CLAMP_INT iStaminaTimer 0 100 (iStaminaTimer)
        SET_SCRIPT_VAR pScript 8 iStaminaTimer
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF
    
    // Stamina script doesn't exist yet, keep running this script to control the timer
    SCRIPT_NAME ISSTMBO
    
    GET_SCRIPT_STRUCT_NAMED InvSyst (pRootScript)
    IF pRootScript > 0x0
        GET_SCRIPT_VAR pRootScript VAR_ITEMS_NAMES lItemsNames
        READ_STRUCT_PARAM pStoredItem STORED_ITEM_ID (i)
        GET_LIST_STRING_VALUE_BY_INDEX lItemsNames i tItemName 

        GOSUB GetDefaultStamina
        GOSUB SetStamina

        SET_SCRIPT_EVENT_SAVE_CONFIRMATION ON OnSaveConfirmation iSaveSlot

        iStaminaTimer =# fStaminaTime
        DISPLAY_ONSCREEN_COUNTER_WITH_STRING_LOCAL iStaminaTimer COUNTER_DISPLAY_BAR $tItemName

        WHILE iStaminaTimer > 0
            WAIT 1000
            iStaminaTimer -= 1
            GOSUB SetStamina
        ENDWHILE

        GOSUB SetDefaultStamina
        CLEAR_ONSCREEN_COUNTER_LOCAL iStaminaTimer
    ENDIF

    TERMINATE_THIS_CUSTOM_SCRIPT

    GetDefaultStamina:
    READ_STRUCT_OFFSET 0xB7CD98 0x14C 1 (iDefaultInfiniteRun)
    READ_STRUCT_OFFSET 0xB7CD98 0x14D 1 (iDefaultFastReload)
    RETURN

    SetStamina:
    SET_PLAYER_NEVER_GETS_TIRED iPlayerId ON
    SET_PLAYER_FAST_RELOAD iPlayerId ON
    RETURN

    SetDefaultStamina:
    SET_PLAYER_NEVER_GETS_TIRED iPlayerId iDefaultInfiniteRun
    SET_PLAYER_FAST_RELOAD iPlayerId iDefaultFastReload
    RETURN

    OnSaveConfirmation:
    // Reset, so will not be saved into saved game
    GOSUB SetDefaultStamina
    // Game passed time, no more coffee effect (need to check if really passed time)
    //iStaminaTimer = 0
    RETURN_SCRIPT_EVENT
}
SCRIPT_END