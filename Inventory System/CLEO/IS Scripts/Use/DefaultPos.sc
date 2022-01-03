// by Junior_Djjr - MixMods.com.br
// You need: https://forum.mixmods.com.br/f141-gta3script-cleo/t5206-como-criar-scripts-com-cleo
SCRIPT_START
{
    // In: Char handle and pointer to stored item
    LVAR_INT hChar pStoredItem
    // In: Your values configured in .ini file, MUST RECEIVE AS FLOAT, if you need INT, just convert it (i =# f).
    LVAR_FLOAT fGroundHeight fRotX fRotY fRotZ fCollision

    // Your variables definition
    LVAR_INT i iItemID pItemData hObject phObject bOk bIsLeftHand bIsObjectBellow
    LVAR_FLOAT f x y z charX charY charZ

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

    // External actions
    CONST_INT EXTERNAL_ACTION_NONE 0
    CONST_INT EXTERNAL_ACTION_CREATE_ITEM_OBJECT 1
    CONST_INT EXTERNAL_ACTION_GIVE_ITEM 2

    // End script if not called by script root, or wrong char arg
    IF hChar = 0
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    // -----------------------------
    // Your code called when using the item

    fGroundHeight /= 100.0 // convert from centimeters

    // Get drop position, avoiding loosing item
    IF GOSUB GetDropPosition

        // Create item object
        READ_STRUCT_PARAM pStoredItem STORED_ITEM_DATA (pItemData)
        READ_STRUCT_PARAM pItemData DATA_ITEM_ID (iItemID)

        READ_STRUCT_PARAM pItemData DATA_BONE i
        IF i <= 24
        OR i >= 26
            TASK_PLAY_ANIM_SECONDARY hChar IS_PICK_L INVENTORYSYSTEM 3.0 0 0 0 0 -1
            TASK_PLAY_ANIM_SECONDARY hChar IS_PICK_L INVENTORYSYSTEM 3.0 0 0 0 0 -1
            bIsLeftHand = TRUE
        ELSE
            TASK_PLAY_ANIM_SECONDARY hChar IS_PICK_R INVENTORYSYSTEM 3.0 0 0 0 0 -1
            TASK_PLAY_ANIM_SECONDARY hChar IS_PICK_R INVENTORYSYSTEM 3.0 0 0 0 0 -1
        ENDIF
        TASK_LOOK_AT_COORD hChar x y z 1000
        IF GOSUB IsPickingObjectBellow
            TASK_TOGGLE_DUCK hChar TRUE
            bIsObjectBellow = TRUE
        ENDIF

        bOk = TRUE
        timera = 0
        f = 0.0
        WHILE f < 0.5
            WAIT 0
            IF NOT DOES_CHAR_EXIST hChar
            OR timera > 3000
                bOk = FALSE
                BREAK
            ENDIF
            IF IS_CHAR_DOING_ANY_IMPORTANT_TASK hChar INCLUDE_ANIMS_NONE
                bOk = FALSE
                BREAK
            ENDIF
            IF NOT IS_CHAR_PLAYING_ANIM hChar IS_PICK_L
            AND NOT IS_CHAR_PLAYING_ANIM hChar IS_PICK_R
                bOk = FALSE
                BREAK
            ENDIF
            IF bIsLeftHand = TRUE
                GET_CHAR_ANIM_CURRENT_TIME hChar IS_PICK_L f
            ELSE
                GET_CHAR_ANIM_CURRENT_TIME hChar IS_PICK_R f
            ENDIF
        ENDWHILE
        
        IF DOES_CHAR_EXIST hChar

            IF bIsObjectBellow = TRUE
                TASK_TOGGLE_DUCK hChar FALSE
            ENDIF

            IF bOk = TRUE
                GET_DISTANCE_BETWEEN_COORDS_3D x y z charX charY charZ (f)
                IF f > 1.0
                    IF GOSUB GetDropPosition
                    ELSE
                        bOk = FALSE
                    ENDIF
                ENDIF

                IF bOk = TRUE
                    GET_VAR_POINTER hObject (phObject)

                    i = EXTERNAL_ACTION_CREATE_ITEM_OBJECT //inVar1: pointer to object handle return, inVar2: item name or ID
                    STREAM_CUSTOM_SCRIPT "Inventory System (Junior_Djjr).cs" i phObject iItemID
                    WAIT 0 // Wait for process

                    IF DOES_OBJECT_EXIST hObject
                        SET_OBJECT_COORDINATES hObject x y z
                        SET_OBJECT_ROTATION hObject fRotX fRotY fRotZ
                        IF fCollision = 1.0
                            SET_OBJECT_COLLISION hObject OFF
                        ENDIF

                        GOSUB DecreaseCount
                        GOSUB HideObject
                    ENDIF
                ENDIF
            ENDIF
        ENDIF
    ENDIF

    GOSUB StopUsing

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
    
    IsPickingObjectBellow:
    charZ -= z
    //PRINT_FORMATTED_NOW "%f" 1000 charZ
    IF charZ > 0.4
        RETURN_TRUE
    ELSE
        RETURN_FALSE
    ENDIF
    RETURN

    GetDropPosition:
    GET_CHAR_COORDINATES hChar charX charY charZ
    charZ += 0.4
    GET_OFFSET_FROM_CHAR_IN_WORLD_COORDS hChar 0.0 0.8 0.6 (x y z) // ground in front of char
    GET_GROUND_Z_FOR_3D_COORD x y z (f)
    f += 0.1
    IF f = 0.1
    OR NOT IS_LINE_OF_SIGHT_CLEAR x y f charX charY charZ 1 0 0 0 0
        GET_OFFSET_FROM_CHAR_IN_WORLD_COORDS hChar 0.0 0.5 0.0 (x y z) // try again a bit closer
        GET_GROUND_Z_FOR_3D_COORD x y z (f)
        f += 0.1
        IF f = 0.1
        OR NOT IS_LINE_OF_SIGHT_CLEAR x y f charX charY charZ 1 0 0 0 0
            // can't get pos in front of char, use char position
            x = charX
            y = charY
            z = charZ
            GET_GROUND_Z_FOR_3D_COORD x y z (f)
            f += 0.1
            IF f = 0.1
                f = charZ + 0.1 // can't find ground, use char position
            ENDIF
        ENDIF
    ENDIF
    f -= 0.1
    z = f + fGroundHeight
    charZ -= 0.4
    GET_DISTANCE_BETWEEN_COORDS_3D x y z charX charY charZ (f)
    IF f > 2.0
        RETURN_FALSE
    ELSE
        RETURN_TRUE
    ENDIF
    RETURN
}
SCRIPT_END
