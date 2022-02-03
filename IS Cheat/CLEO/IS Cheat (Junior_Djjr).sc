SCRIPT_START
{
    LVAR_INT scplayer i iTypedChars pBuffer lItemsNames iTotalItemsNames iChar bFound iLength
    LVAR_TEXT_LABEL tItemName

    //-----------------

    // Vars from root script that can be accessed from other scripts
    CONST_INT VAR_INVENTORY_SPACES 1 // pointer to stored items array
    CONST_INT VAR_ITEMS_NAMES 2 // items name list (each name is unique, item ID is the list index)
    CONST_INT VAR_ITEMS_DATA 3 // items data list
    CONST_INT VAR_ITEMS_SCRIPTS 4 // items scripts, values and icons list
    CONST_INT VAR_SELECTED_STORED_ITEM 5 // current stored item
    CONST_INT VAR_SELECTED_SLOT 6 // current slot
    CONST_INT VAR_USING_STORED_ITEM 7 // current stored item in use, 0 if not using item
    CONST_INT VAR_SELECTING_USE 8 // boolean is selecting use (you can use it to show some screen instruction etc)

    // External actions
    CONST_INT EXTERNAL_ACTION_NONE 0
    CONST_INT EXTERNAL_ACTION_CREATE_ITEM_OBJECT 1
    CONST_INT EXTERNAL_ACTION_GIVE_ITEM 2

    //-----------------

    WAIT 0
    WAIT 0
    WAIT 0 // to give time for items to be read on script root

    GET_PLAYER_CHAR 0 (scplayer)
    
    GET_SCRIPT_STRUCT_NAMED InvSyst (i)
    IF i > 0x0
        GET_THREAD_VAR i VAR_ITEMS_NAMES lItemsNames
        GET_LIST_SIZE lItemsNames (iTotalItemsNames)
    ELSE
        //PRINT_STRING_NOW "~r~No Inventory System script installed." 5000
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    GET_LABEL_POINTER BufferItemGive (pBuffer)

    WHILE TRUE
        WAIT 0

        IF TEST_CHEAT "ISCHEAT"
            SET_PLAYER_CONTROL_PAD_MOVEMENT PAD1 OFF
            WRITE_MEMORY pBuffer 8 0x0 FALSE
            iTypedChars = 0
            WHILE TRUE
                WAIT 0
                IF iTypedChars < 8
                    READ_MEMORY 0x969110 1 FALSE (iChar)
                    IF iChar > 0x0 //not null terminator
                        WRITE_STRUCT_OFFSET pBuffer iTypedChars 1 (iChar)
                        WRITE_MEMORY 0x969110 1 0x0 FALSE //null terminator
                        ++iTypedChars
                        GOSUB CheckSpawnName
                    ENDIF
                ELSE
                    WRITE_MEMORY 0x969110 1 0x0 FALSE //null terminator
                ENDIF
                IF IS_KEY_JUST_PRESSED VK_BACK
                    iTypedChars -= 1
                    WRITE_STRUCT_OFFSET pBuffer iTypedChars 1 (0x0)
                    GOSUB CheckSpawnName
                ENDIF
                IF bFound = TRUE
                    PRINT_FORMATTED_NOW "'%s'~n~~y~(Press Enter)" 100 $pBuffer
                    IF IS_KEY_PRESSED VK_RETURN
                        // Give item to player
                        i = EXTERNAL_ACTION_GIVE_ITEM //char; item name pointer or ID
                        IF STREAM_CUSTOM_SCRIPT "Inventory System (Junior_Djjr).cs" i scplayer pBuffer
                        ELSE
                            PRINT_STRING_NOW "~r~Fail to give item." 1000
                        ENDIF
                        BREAK
                    ENDIF
                ELSE
                    PRINT_FORMATTED_NOW "'%s'" 100 $pBuffer
                    IF IS_KEY_PRESSED VK_RETURN
                        BREAK
                    ENDIF
                ENDIF
            ENDWHILE
            WAIT 500
            /*REQUEST_ANIMATION VENDING
            LOAD_ALL_MODELS_NOW
            WHILE TRUE
                WAIT 0
                TASK_PLAY_ANIM_NON_INTERRUPTABLE scplayer VEND_Drink_P VENDING 2.0 FALSE FALSE FALSE FALSE -1
                SET_CHAR_ANIM_CURRENT_TIME scplayer VEND_Drink_P 0.7
                SET_CHAR_ANIM_PLAYING_FLAG scplayer VEND_Drink_P OFF
            ENDWHILE*/
            SET_PLAYER_CONTROL_PAD_MOVEMENT PAD1 ON
        ENDIF

    ENDWHILE

    CheckSpawnName:
    bFound = FALSE
    i = 0
    WHILE i < iTotalItemsNames
        GET_LIST_STRING_VALUE_BY_INDEX lItemsNames i (tItemName)
        GET_STRING_LENGTH $tItemName (iLength)
        IF iLength = iTypedChars
            IF IS_STRING_EQUAL $tItemName $pBuffer 7 FALSE ""
                bFound = TRUE
            ENDIF
        ENDIF
        ++i
    ENDWHILE
    RETURN

}
SCRIPT_END

BufferItemGive:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //64
ENDDUMP