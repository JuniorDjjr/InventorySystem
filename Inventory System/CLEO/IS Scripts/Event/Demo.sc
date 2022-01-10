SCRIPT_START
{
    // In: Char handle, pointer to stored item and event ID
    LVAR_INT hChar pStoredItem iEventID
    // In: Your values configured in .ini file, MUST RECEIVE AS FLOAT, if you need INT, just convert it (i =# f).
    LVAR_FLOAT fExample

    // Your variables definition
    LVAR_INT i
    LVAR_FLOAT f

    // Constants - Prefer using them for read only

    // Item stored struct params IDs
    CONST_INT STORED_ITEM_ID 0
    CONST_INT STORED_ITEM_DATA 1
    CONST_INT STORED_COUNT 2
    CONST_INT STORED_STATE 3
    
    // Item data struct param IDs
    CONST_INT DATA_ITEM_ID 0
    CONST_INT DATA_MODEL_ID 1
    CONST_INT DATA_BONE 2
    CONST_INT DATA_STACK 3

    // Events
    CONST_INT EVENT_ITEM_AFTER_GIVE 0
    CONST_INT EVENT_ITEM_AFTER_INCREASE_COUNT 1
    CONST_INT EVENT_ITEM_AFTER_DECREASE_COUNT 2
    CONST_INT EVENT_ITEM_BEFORE_CLEAR 3

    // End script if not called by script root, or wrong char arg
    IF hChar = 0
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    // -----------------------------
    // Your code called for each event ID

    IF NOT DOES_CHAR_EXIST hChar
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    SWITCH iEventID
        CASE EVENT_ITEM_AFTER_GIVE
            PRINT_STRING_NOW "EVENT_ITEM_AFTER_GIVE" 1000
            BREAK
        CASE EVENT_ITEM_AFTER_INCREASE_COUNT
            PRINT_STRING_NOW "EVENT_ITEM_AFTER_INCREASE_COUNT" 1000
            BREAK
        CASE EVENT_ITEM_AFTER_DECREASE_COUNT
            PRINT_STRING_NOW "EVENT_ITEM_AFTER_DECREASE_COUNT" 1000
            BREAK
        CASE EVENT_ITEM_BEFORE_CLEAR
            PRINT_STRING_NOW "EVENT_ITEM_BEFORE_CLEAR" 1000
            BREAK
    ENDSWITCH

}
SCRIPT_END
