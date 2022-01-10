// by Junior_Djjr - MixMods.com.br
// You need: https://forum.mixmods.com.br/f141-gta3script-cleo/t5206-como-criar-scripts-com-cleo
SCRIPT_START
{
    LVAR_INT iExternalAction //In (note: this script called from external will not use any variable names bellow, it's a new scope)
    LVAR_INT pInventorySpaces lItemsNames lItemsData lItemsScripts pSelectedStoredItem iSelectedSlot pUsingStoredItem // Can be accessed from other scripts, order must preserve
    LVAR_INT scplayer bInventoryOpen pDragStoredItem hRenderObjectItem iCurrentUseMode iNewUseMode hObjectPicking i j
    LVAR_INT iLastSelectedSlot iSelectedItemID iLastSelectedItemID bSelectingUse bLastSelectingUse 
    LVAR_FLOAT f x y z x2 y2 xSel ySel

    CONST_INT MOD_VERSION 2

    // --- START OF constants header that you can copy and use for your item scripts

    // Vars from root script that can be accessed from other scripts
    CONST_INT VAR_INVENTORY_SPACES 1 // pointer to stored items array
    CONST_INT VAR_ITEMS_NAMES 2 // items name list (each name is unique, item ID is the list index)
    CONST_INT VAR_ITEMS_DATA 3 // items data list
    CONST_INT VAR_ITEMS_SCRIPTS 4 // items scripts, values and icons list
    CONST_INT VAR_SELECTED_STORED_ITEM 5 // current stored item
    CONST_INT VAR_SELECTED_SLOT 6 // current slot
    CONST_INT VAR_USING_STORED_ITEM 7 // current stored item in use, 0 if not using item
    CONST_INT VAR_SELECTING_USE 8 // boolean is selecting use (you can use it to show some screen instruction etc)

    // Item stored struct params IDs
    CONST_INT STORED_ITEM_ID 0
    CONST_INT STORED_ITEM_DATA 1
    CONST_INT STORED_COUNT 2
    CONST_INT STORED_FLAGS 3
    CONST_INT STORED_TOTAL_PARAMS 4

    // Item flags
    CONST_INT ITEM_FLAG_USING 0
    CONST_INT ITEM_FLAG_DECREASE_COUNT 1
    CONST_INT ITEM_FLAG_HIDE 2
    
    // Item data struct param IDs
    CONST_INT DATA_ITEM_ID 0
    CONST_INT DATA_MODEL_ID 1
    CONST_INT DATA_BONE 2
    CONST_INT DATA_STACK 3
    CONST_INT DATA_OFFSET_X 4
    CONST_INT DATA_OFFSET_Y 5
    CONST_INT DATA_OFFSET_Z 6
    CONST_INT DATA_ROTATION_X 7
    CONST_INT DATA_ROTATION_Y 8
    CONST_INT DATA_ROTATION_Z 9
    CONST_INT DATA_TOTAL_PARAMS 10

    // External actions
    CONST_INT EXTERNAL_ACTION_NONE 0
    CONST_INT EXTERNAL_ACTION_CREATE_ITEM_OBJECT 1
    CONST_INT EXTERNAL_ACTION_GIVE_ITEM 2

    // Object extended vars
    CONST_INT EXTENDED_VAR_ITEM_DATA 1
    CONST_INT EXTENDED_VAR_TOTAL 1

    // --- END OF constants header that you can copy and use on your item scripts

    // Use modes
    CONST_INT USE_NONE 0
    CONST_INT USE_MAIN 1
    CONST_INT USE_POSITION 2
    CONST_INT USE_EXTERNAL_A 3
    CONST_INT USE_EXTERNAL_B 4
    CONST_INT USE_EVENT 5

    // Events
    CONST_INT EVENT_ITEM_AFTER_GIVE 0
    CONST_INT EVENT_ITEM_AFTER_INCREASE_COUNT 1
    CONST_INT EVENT_ITEM_AFTER_DECREASE_COUNT 2
    CONST_INT EVENT_ITEM_BEFORE_CLEAR 3

    // Use scripts
    CONST_INT SCRIPT_USE_MAIN_STRING_INDEX 0
    CONST_INT SCRIPT_USE_POS_STRING_INDEX 3
    CONST_INT SCRIPT_USE_A_STRING_INDEX 6
    CONST_INT SCRIPT_USE_B_STRING_INDEX 9
    CONST_INT SCRIPT_USE_EVENT_STRING_INDEX 12
    CONST_INT SCRIPT_TOTAL_USE_STRINGS_EACH_ITEM 14

    CONST_INT SCRIPT_USE_SCRIPT 0
    CONST_INT SCRIPT_USE_VALUES 1
    CONST_INT SCRIPT_USE_ICON 2

    CONST_FLOAT SELECT_USE_POS_X 320.0
    CONST_FLOAT SELECT_USE_POS_Y 200.0
    CONST_FLOAT SELECT_USE_OFFSET_X 50.0
    CONST_FLOAT SELECT_USE_OFFSET_Y 50.0
    CONST_FLOAT SELECT_USE_ITEM_SIZE 32.0

    CONST_FLOAT ITEM_OBJECT_MIN_DISTANCE_TO_SHOW 4.0
    CONST_FLOAT ITEM_OBJECT_MIN_DISTANCE_TO_PICKUP 1.3
    CONST_FLOAT ITEM_OBJECT_NAME_OFFSET 0.3
    CONST_FLOAT ITEM_OBJECT_BUTTON_OFFSET 0.4
    CONST_FLOAT ITEM_OBJECT_TEXT_SIZE_MULT 0.4


    // Inventory spaces (changing requires edit script memory space and other tweaks)
    CONST_INT INVENTORY_TOTAL_COL 9
    CONST_INT INVENTORY_TOTAL_ROW 3
    CONST_INT INVENTORY_TOTAL_SPACES 27 //INVENTORY_TOTAL_COL*INVENTORY_TOTAL_ROW

    // Inventory window
    CONST_FLOAT INVENTORY_WINDOW_ITEM_SIZE 28.0
    CONST_FLOAT INVENTORY_WINDOW_ITEM_SIZE_INVERTED -28.0
    CONST_FLOAT INVENTORY_WINDOW_ITEM_SIZE_SELECTED 32.0
    CONST_FLOAT INVENTORY_WINDOW_ROW_SIZE 32.0
    CONST_FLOAT INVENTORY_WINDOW_COL_SIZE 32.0
    CONST_FLOAT INVENTORY_WINDOW_POS_X 224.0
    CONST_FLOAT INVENTORY_WINDOW_POS_Y 300.0
    CONST_FLOAT INVENTORY_WINDOW_BACK_POS_X 320.0
    CONST_FLOAT INVENTORY_WINDOW_BACK_POS_Y 330.0
    CONST_FLOAT INVENTORY_WINDOW_BACK_SIZE_X 300.0
    CONST_FLOAT INVENTORY_WINDOW_BACK_SIZE_Y 120.0

    // Cursor
    CONST_FLOAT CURSOR_SIZE_X -16.0
    CONST_FLOAT CURSOR_SIZE_Y 16.0
    CONST_FLOAT CURSOR_SENSIBILITY 1.5
    CONST_FLOAT CURSOR_NON_ACCELERATION 120.0

    IF iExternalAction > 0
        IF iExternalAction = EXTERNAL_ACTION_CREATE_ITEM_OBJECT
            GOSUB RunExternalAction_CreateItemObject
        ENDIF
        IF iExternalAction = EXTERNAL_ACTION_GIVE_ITEM
            GOSUB RunExternalAction_GiveItem
        ENDIF
        TERMINATE_THIS_CUSTOM_SCRIPT
    ENDIF

    SCRIPT_NAME InvSyst

    WAIT 0
    WAIT 0

    LOAD_TEXTURE_DICTIONARY InvSyst
    LOAD_SPRITE 1 "cursor"

    CONST_INT SPRITE_USE_START_ID 2
    i = SPRITE_USE_START_ID
    LOAD_SPRITE i "Use"
    ++i
    LOAD_SPRITE i "Pos"
    ++i
    LOAD_SPRITE i "Eat"
    ++i
    LOAD_SPRITE i "Drink"
    ++i
    LOAD_SPRITE i "Money"

    REQUEST_ANIMATION INVENTORYSYSTEM
    LOAD_ALL_MODELS_NOW
    
    GET_PLAYER_CHAR 0 scplayer

    CREATE_LIST DATATYPE_STRING lItemsNames
    CREATE_LIST DATATYPE_STRING lItemsScripts
    CREATE_LIST DATATYPE_INT lItemsData
    GET_LABEL_POINTER InventorySpaces pInventorySpaces

    CLEO_CALL InitInventory 0 (pInventorySpaces)()
    CLEO_CALL AllocateDragStoredItem 0 ()(pDragStoredItem)

    CLEO_CALL ReadStoreItemsData 0 (lItemsNames lItemsData lItemsScripts)()

    SET_SCRIPT_EVENT_SAVE_CONFIRMATION ON OnSaveConfirmation i

    CLEO_CALL LoadSave 0 (scplayer pInventorySpaces lItemsNames lItemsData lItemsScripts)(iSelectedSlot)

    timera = 99999
    iCurrentUseMode = USE_NONE
    iLastSelectedItemID = -1
    iLastSelectedSlot = -1
    bLastSelectingUse = -1
    hRenderObjectItem = 0

    WHILE TRUE
        WAIT 0

        /*IF TEST_CHEAT OB
            REQUEST_MODEL 3156
            LOAD_ALL_MODELS_NOW
            GET_OFFSET_FROM_CHAR_IN_WORLD_COORDS scplayer 0.0 1.0 0.1 x y z
            GET_GROUND_Z_FOR_3D_COORD x y z (z)
            z += 0.04
            CREATE_OBJECT_NO_SAVE 3156 x y z FALSE FALSE hObject
        ENDIF*/

        /*IF TEST_CHEAT SAV
            ACTIVATE_SAVE_MENU
        ENDIF

        IF TEST_CHEAT BU

            GET_LABEL_POINTER ShortBuffer i
            COPY_STRING "ISburgr" i
            IF CLEO_CALL GiveItem 0 (scplayer i pInventorySpaces lItemsNames lItemsData lItemsScripts -1)(i)
                //PRINT_STRING "OK" 100
            ELSE
                PRINT_FORMATTED_NOW "ERROR: Can't give item, code %i" 3000 i
                WAIT 3000
            ENDIF

        ENDIF*/

        IF pSelectedStoredItem > 0x0
        AND pUsingStoredItem = 0x0 
            IF TEST_CHEAT ISITEMEDIT
                GOSUB CloseInventory
                CLEO_CALL ItemEdit 0 (scplayer pSelectedStoredItem lItemsNames hRenderObjectItem)(hRenderObjectItem)
            ENDIF
        ENDIF

        //__int16 __thiscall CPad::GetDisplayVitalStats(CPad *this, CPed *a2)
        GET_PED_POINTER scplayer (i)
        CALL_METHOD_RETURN 0x5408B0 0xB73458 1 0 (i)(i)
        IF i = TRUE
        AND NOT IS_CHAR_IN_ANY_CAR scplayer
            IF bInventoryOpen = FALSE
                GOSUB OpenInventory
            ENDIF
        ELSE
            IF bInventoryOpen = TRUE
                GOSUB CloseInventory
            ENDIF
        ENDIF

        GOSUB CycleSlots

        IF bInventoryOpen = TRUE
            CLEO_CALL ProcessInventory 0 (1 pInventorySpaces lItemsNames pDragStoredItem iSelectedSlot scplayer lItemsScripts)
        ELSE
            IF timera < 1000
                CLEO_CALL ProcessInventory 0 (2 pInventorySpaces lItemsNames pDragStoredItem iSelectedSlot scplayer lItemsScripts)
            ELSE
                // not showing inventory
                CLEO_CALL ProcessAllItemObjects 0 (scplayer pInventorySpaces lItemsNames lItemsData lItemsScripts hObjectPicking)(hObjectPicking)
            ENDIF
        ENDIF

        GET_CURRENT_CHAR_WEAPON scplayer (i)
        IF i > 1
            IF pSelectedStoredItem > 0x0
                READ_STRUCT_PARAM pSelectedStoredItem STORED_ITEM_DATA i
                CLEO_CALL CanHoldItem 0 (scplayer i)(j)
            ELSE
                j = FALSE
            ENDIF
        ELSE
            j = TRUE
        ENDIF

        // Update selected item and use
        bSelectingUse = FALSE

        IF iSelectedSlot > -1 // any slot selected
        AND j = TRUE // can hold item

            IF pUsingStoredItem = 0x0 // no item in use

                // Select new use
                READ_STRUCT_PARAM pInventorySpaces iSelectedSlot (pSelectedStoredItem)
                READ_STRUCT_PARAM pSelectedStoredItem STORED_ITEM_ID (iSelectedItemID)

                IF bInventoryOpen = FALSE
                    IF iSelectedItemID > -1
                        IF IS_AIM_BUTTON_PRESSED PAD1
                        AND NOT IS_CHAR_DOING_ANY_IMPORTANT_TASK scplayer INCLUDE_ANIMS_NONE

                            IF CLEO_CALL IsAnyUseForItem 0 (iSelectedItemID lItemsScripts)()
                                GOSUB ProcessSelectUse
                            ENDIF
                        ENDIF
                    ENDIF
                ENDIF
            ENDIF

            // Update selected item
            IF NOT iSelectedItemID = iLastSelectedItemID // different item selected
                GOSUB PartialUnselectItem
                IF iSelectedItemID > -1 // has item selected
                AND pUsingStoredItem = 0x0 // no item in use
                    CLEO_CALL SelectItem 0 (pSelectedStoredItem hRenderObjectItem)(hRenderObjectItem)
                ENDIF
                iLastSelectedItemID = iSelectedItemID
            ENDIF
            
        ELSE
            // Can't hold item now
            IF iLastSelectedSlot > -1 // was slot selected last frame
                GOSUB UnselectItem
            ENDIF
        ENDIF

        IF pUsingStoredItem > 0x0
            IF NOT pSelectedStoredItem = pUsingStoredItem
                PRINT_STRING_NOW "~y~Warning: Selected item is different from using item." 1000
            ENDIF
        ENDIF

        // Update flags for selected item
        IF pSelectedStoredItem > 0x0
            READ_STRUCT_PARAM pSelectedStoredItem STORED_FLAGS (j)
            IF IS_LOCAL_VAR_BIT_SET_CONST j ITEM_FLAG_DECREASE_COUNT
                IF CLEO_CALL DecreaseItemCount 0 (scplayer pSelectedStoredItem 1 lItemsScripts)()
                    // Still exists
                    CLEAR_LOCAL_VAR_BIT_CONST j ITEM_FLAG_DECREASE_COUNT
                    WRITE_STRUCT_PARAM pSelectedStoredItem STORED_FLAGS j
                    i = EVENT_ITEM_AFTER_DECREASE_COUNT
                    CLEO_CALL StartUseOrEvent 0 (scplayer pSelectedStoredItem 0 lItemsScripts i)()
                ELSE
                    // Not exists anymore
                    GOSUB UnselectItem
                ENDIF
            ENDIF
            IF hRenderObjectItem > 0x0
                IF IS_LOCAL_VAR_BIT_SET_CONST j ITEM_FLAG_HIDE
                OR NOT IS_PLAYER_PLAYING 0
                OR NOT IS_PLAYER_CONTROL_ON 0
                OR IS_ON_SCRIPTED_CUTSCENE
                    i = FALSE
                ELSE
                    IF NOT CAN_PLAYER_START_MISSION 0
                        // Exceptions for CAN_PLAYER_START_MISSION (not totally correct but it's ok)
                        IF IS_CHAR_IN_AIR scplayer
                            i = TRUE
                        ELSE
                            i = FALSE
                        ENDIF
                    ENDIF
                    i = TRUE
                ENDIF
                SET_RENDER_OBJECT_VISIBLE hRenderObjectItem i
            ENDIF
        ELSE
            // just to make sure it's deleted (bug sometimes, need fix it)
            IF NOT hRenderObjectItem = 0
                DELETE_RENDER_OBJECT hRenderObjectItem
                hRenderObjectItem = 0
            ENDIF
        ENDIF 
        

        // Update flags for using item (normally is the same as pSelectedStoredItem, but just to make sure)
        IF pUsingStoredItem > 0x0
            READ_STRUCT_PARAM pUsingStoredItem STORED_FLAGS (i)
            IF NOT IS_LOCAL_VAR_BIT_SET_CONST i ITEM_FLAG_USING
                WRITE_STRUCT_PARAM pUsingStoredItem STORED_FLAGS 0 // clear all flags, to simulate a new item instance
                pUsingStoredItem = 0
            ENDIF
        ENDIF


        IF bSelectingUse = FALSE
            IF bLastSelectingUse = TRUE
                SET_CAMERA_CONTROL TRUE
                iCurrentUseMode = iNewUseMode
                IF iCurrentUseMode = USE_NONE
                    // just selected none
                ELSE
                    // just selected use
                    IF CLEO_CALL StartUseOrEvent 0 (scplayer pSelectedStoredItem iCurrentUseMode lItemsScripts -1)()
                        pUsingStoredItem = pSelectedStoredItem
                    ENDIF
                ENDIF
            ENDIF
            xSel = 0.0
            ySel = 0.0
        ENDIF

        bLastSelectingUse = bSelectingUse
        iLastSelectedSlot = iSelectedSlot

    ENDWHILE

    UnselectItem:
    pSelectedStoredItem = 0
    iLastSelectedItemID = -1
    iSelectedItemID = -1
    PartialUnselectItem:
    IF NOT hRenderObjectItem = 0
        DELETE_RENDER_OBJECT hRenderObjectItem
        hRenderObjectItem = 0
    ENDIF
    RETURN

    OpenInventory:
    //PRINT_STRING_NOW "open inventory" 500
    SET_CAMERA_CONTROL FALSE
    SET_PLAYER_CONTROL_PAD_MOVEMENT PAD1 OFF
    bInventoryOpen = TRUE
    timera = 99999
    MAKE_NOP 0x5408B3 8
    CLEO_CALL CursorInit 0 (320.0 280.0)
    RETURN

    CloseInventory:
    //PRINT_STRING_NOW "close inventory" 500
    SET_CAMERA_CONTROL TRUE
    SET_PLAYER_CONTROL_PAD_MOVEMENT PAD1 ON
    bInventoryOpen = FALSE
    timera = 99999
    GET_LABEL_POINTER Original_5408B3_Bytes (i)
    COPY_MEMORY i 0x5408B3 8
    RETURN

    CycleSlots:
    IF IS_KEY_JUST_PRESSED VK_KEY_1
        iSelectedSlot = 0
        timera = 0
    ENDIF
    IF IS_KEY_JUST_PRESSED VK_KEY_2
        iSelectedSlot = 1
        timera = 0
    ENDIF
    IF IS_KEY_JUST_PRESSED VK_KEY_3
        iSelectedSlot = 2
        timera = 0
    ENDIF
    IF IS_KEY_JUST_PRESSED VK_KEY_4
        iSelectedSlot = 3
        timera = 0
    ENDIF
    IF IS_KEY_JUST_PRESSED VK_KEY_5
        iSelectedSlot = 4
        timera = 0
    ENDIF
    IF IS_KEY_JUST_PRESSED VK_KEY_6
        iSelectedSlot = 5
        timera = 0
    ENDIF
    IF IS_KEY_JUST_PRESSED VK_KEY_7
        iSelectedSlot = 6
        timera = 0
    ENDIF
    IF IS_KEY_JUST_PRESSED VK_KEY_8
        iSelectedSlot = 7
        timera = 0
    ENDIF
    IF IS_KEY_JUST_PRESSED VK_KEY_9
        iSelectedSlot = 8
        timera = 0
    ENDIF
    IF IS_KEY_JUST_PRESSED VK_KEY_0
    OR IS_KEY_JUST_PRESSED 192 //'/`
        iSelectedSlot = -1
        timera = 0
    ENDIF
    /*IF IS_MOUSE_WHEEL_DOWN
        GOSUB CycleSlotUp
        timera = 0
    ENDIF
    IF IS_MOUSE_WHEEL_UP
        GOSUB CycleSlotDown
        timera = 0
    ENDIF*/
    RETURN

    CycleSlotUp:
    ++iSelectedSlot
    IF iSelectedSlot >= INVENTORY_TOTAL_COL
        iSelectedSlot = -1
    ENDIF
    RETURN

    CycleSlotDown:
    --iSelectedSlot
    IF iSelectedSlot < -1
        iSelectedSlot = INVENTORY_TOTAL_COL - 1
    ENDIF
    RETURN

    ProcessSelectUse:
    bSelectingUse = TRUE

    SET_CAMERA_CONTROL FALSE

    GET_PC_MOUSE_MOVEMENT x y
    IF NOT IS_MOUSE_USING_VERTICAL_INVERSION 
        y *= -1.0
    ENDIF
    GET_FIXED_XY_ASPECT_RATIO x y (x y)
    GET_MOUSE_SENSIBILITY f
    f *= CURSOR_SENSIBILITY
    f *= 8.0
    x *= f
    y *= f
    xSel += x
    ySel += y

    x *= 0.5
    y *= 0.5

    ABS_LVAR_FLOAT x
    ABS_LVAR_FLOAT y

    IF xSel > 0.0
        xSel -= y
    ELSE
        xSel += y
    ENDIF
    IF ySel > 0.0
        ySel -= x
    ELSE
        ySel += x
    ENDIF

    //PRINT_FORMATTED_NOW "%f %f" 1000 xSel ySel

    iNewUseMode = USE_NONE

    GET_FIXED_XY_ASPECT_RATIO SELECT_USE_OFFSET_X SELECT_USE_OFFSET_Y (x2 y2)

    x = SELECT_USE_POS_X + 0.0
    y = SELECT_USE_POS_Y - y2

    DRAW_TEXTURE_PLUS 0 DRAW_EVENT_BEFORE_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (0 0 0 160)
    IF ySel > 1.0
        iNewUseMode = USE_MAIN
        DRAW_TEXTURE_PLUS 0 DRAW_EVENT_BEFORE_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (40 140 0 255)
    ENDIF
    CLEO_CALL DrawUseIcon 0 (1 x y iSelectedItemID lItemsNames lItemsScripts)

    x = SELECT_USE_POS_X + 0.0
    y = SELECT_USE_POS_Y + y2

    DRAW_TEXTURE_PLUS 0 DRAW_EVENT_BEFORE_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (0 0 0 160)
    IF ySel < -1.0
        iNewUseMode = USE_POSITION
        DRAW_TEXTURE_PLUS 0 DRAW_EVENT_BEFORE_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (40 140 0 255)
    ENDIF
    CLEO_CALL DrawUseIcon 0 (2 x y iSelectedItemID lItemsNames lItemsScripts)

    x = SELECT_USE_POS_X - x2
    y = SELECT_USE_POS_Y + 0.0

    DRAW_TEXTURE_PLUS 0 DRAW_EVENT_BEFORE_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (0 0 0 160)
    IF xSel < -1.0
        iNewUseMode = USE_EXTERNAL_A
        DRAW_TEXTURE_PLUS 0 DRAW_EVENT_BEFORE_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (40 140 0 255)
    ENDIF
    CLEO_CALL DrawUseIcon 0 (3 x y iSelectedItemID lItemsNames lItemsScripts)

    x = SELECT_USE_POS_X + x2
    y = SELECT_USE_POS_Y + 0.0

    DRAW_TEXTURE_PLUS 0 DRAW_EVENT_BEFORE_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (0 0 0 160)
    IF xSel > 1.0
        iNewUseMode = USE_EXTERNAL_B
        DRAW_TEXTURE_PLUS 0 DRAW_EVENT_BEFORE_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (40 140 0 255)
    ENDIF
    CLEO_CALL DrawUseIcon 0 (4 x y iSelectedItemID lItemsNames lItemsScripts)

    x = SELECT_USE_POS_X + 0.0
    y = SELECT_USE_POS_Y + 0.0

    DRAW_TEXTURE_PLUS 0 DRAW_EVENT_BEFORE_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (0 0 0 160)
    IF iNewUseMode = USE_NONE
        DRAW_TEXTURE_PLUS 0 DRAW_EVENT_BEFORE_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (40 140 0 255)
    ENDIF

    CLAMP_FLOAT xSel -1.5 1.5 (xSel)
    CLAMP_FLOAT ySel -1.5 1.5 (ySel)
    RETURN

    OnSaveConfirmation:
    CLEO_CALL Save 0 (i iSelectedSlot pInventorySpaces lItemsNames)()
    RETURN_SCRIPT_EVENT
}

{
    LVAR_INT iExternalAction phObject itemNameOrID //In
    LVAR_INT i lItemsData lItemsNames iItemData pInvSystScript iItemModel hObject

    RunExternalAction_CreateItemObject:
    
    IF NOT phObject = 0x0

        GET_SCRIPT_STRUCT_NAMED InvSyst pInvSystScript
        IF pInvSystScript > 0x0

            // Testing
            GET_THIS_SCRIPT_STRUCT i
            IF pInvSystScript = i
                PRINT_STRING_NOW "~r~Error: Script called is same as root script." 5000
                RETURN
            ENDIF
            
            GET_THREAD_VAR pInvSystScript VAR_ITEMS_DATA lItemsData
            
            IF itemNameOrID > 0xFFFF // is name pointer

                GET_THREAD_VAR pInvSystScript VAR_ITEMS_NAMES lItemsNames
            
                IF CLEO_CALL FindItemDataByName 0 (itemNameOrID lItemsNames lItemsData)(iItemData)
                    GOSUB RunExternalAction_SetupObject
                ELSE
                    PRINT_FORMATTED_NOW "~r~Can't find item named '%s'" 3000 itemNameOrID
                ENDIF
            ELSE
                GET_LIST_VALUE_BY_INDEX lItemsData itemNameOrID (iItemData)
                IF iItemData > 0x0
                    GOSUB RunExternalAction_SetupObject
                ELSE
                    PRINT_FORMATTED_NOW "~r~Can't find item with ID '%i'" 3000 itemNameOrID
                ENDIF
            ENDIF
        ELSE
            PRINT_STRING_NOW "~r~Error: Can't find Inventory System script." 5000
        ENDIF
    ELSE
        PRINT_STRING_NOW "~r~Error: Wrong args for EXTERNAL_ACTION_CREATE_ITEM_OBJECT." 5000
    ENDIF
    RETURN

    RunExternalAction_SetupObject:
    READ_STRUCT_PARAM iItemData DATA_MODEL_ID (iItemModel)
    REQUEST_MODEL iItemModel 
    LOAD_ALL_MODELS_NOW
    CREATE_OBJECT_NO_SAVE iItemModel 0.0 0.0 -50.0 FALSE FALSE (hObject)
    MARK_MODEL_AS_NO_LONGER_NEEDED iItemModel
    INIT_EXTENDED_OBJECT_VARS hObject ISTM EXTENDED_VAR_TOTAL
    SET_EXTENDED_OBJECT_VAR hObject ISTM EXTENDED_VAR_ITEM_DATA iItemData
    WRITE_MEMORY phObject 4 hObject FALSE
    RETURN
}

{
    LVAR_INT iExternalAction hChar itemNameOrID //In
    LVAR_INT i pInventorySpaces lItemsNames lItemsData lItemsScripts pInvSystScript

    RunExternalAction_GiveItem:
    GET_SCRIPT_STRUCT_NAMED InvSyst pInvSystScript
    IF NOT pInvSystScript = 0

        // Testing
        GET_THIS_SCRIPT_STRUCT i
        IF pInvSystScript = i
            PRINT_STRING_NOW "~r~Error: Script called is same as root script." 5000
            RETURN
        ENDIF
        
        GET_THREAD_VAR pInvSystScript VAR_INVENTORY_SPACES pInventorySpaces
        GET_THREAD_VAR pInvSystScript VAR_ITEMS_NAMES lItemsNames
        GET_THREAD_VAR pInvSystScript VAR_ITEMS_DATA lItemsData
        GET_THREAD_VAR pInvSystScript VAR_ITEMS_SCRIPTS lItemsScripts

        IF CLEO_CALL GiveItem 0 (hChar itemNameOrID pInventorySpaces lItemsNames lItemsData lItemsScripts -1)(i)
            //PRINT_STRING "OK" 100
        ELSE
            PRINT_FORMATTED_NOW "Error: Can't give item, code %i" 3000 i
            WAIT 3000
        ENDIF
    ELSE
        PRINT_STRING_NOW "~r~Error: Can't find Inventory System script." 5000
    ENDIF
    RETURN
}

SCRIPT_END

{
    LVAR_INT hChar pInventorySpaces lItemsNames lItemsData lItemsScripts //In
    LVAR_INT i j iSlot pBuffer hFile pStoredItem iCount pShortBuffer iModVersion iSelectedSlot

    LoadSave:
    iSelectedSlot = 0
    GET_CURRENT_SAVE_SLOT iSlot
    GET_LABEL_POINTER ReallyTempBuffer pBuffer
    GET_LABEL_POINTER ShortBuffer pShortBuffer
    STRING_FORMAT pBuffer "modloader\Inventory System\CLEO\IS Saves\%i.issave" iSlot
    IF OPEN_FILE $pBuffer 0x72 hFile

        READ_FROM_FILE hFile 4 iModVersion
        READ_FROM_FILE hFile 4 iSelectedSlot
    
        i = 0
        WHILE i < INVENTORY_TOTAL_SPACES
            READ_STRUCT_PARAM pInventorySpaces i (pStoredItem)
            READ_FROM_FILE hFile 4 iCount //count
            READ_STRING_FROM_FILE hFile pShortBuffer 8 //item name

            IF iCount > 0
                CLEO_CALL GiveItem 0 (hChar pShortBuffer pInventorySpaces lItemsNames lItemsData lItemsScripts pStoredItem)(j)
                WRITE_STRUCT_PARAM pStoredItem STORED_COUNT iCount
            ENDIF

            // Find null terminator
            j = 1
            WHILE NOT j = 0
                READ_FROM_FILE hFile 1 j
            ENDWHILE

            ++i
        ENDWHILE

        CLOSE_FILE hFile
    ENDIF
    CLEO_RETURN 0 (iSelectedSlot)
}

{
    LVAR_INT iSlot iSelectedSlot pInventorySpaces lItemsNames //In
    LVAR_INT i j p pBuffer hFile pStoredItem iItemID pShortBuffer iCount
    LVAR_TEXT_LABEL tItemName

    Save:
    GET_LABEL_POINTER ShortBuffer pShortBuffer
    GET_LABEL_POINTER ReallyTempBuffer pBuffer
    STRING_FORMAT pBuffer "modloader\Inventory System\CLEO\IS Saves\%i.issave" iSlot
    IF OPEN_FILE $pBuffer 0x6277 (hFile)

        i = MOD_VERSION
        WRITE_TO_FILE hFile 4 i
        WRITE_TO_FILE hFile 4 iSelectedSlot
        
        i = 0
        WHILE i < INVENTORY_TOTAL_SPACES
            READ_STRUCT_PARAM pInventorySpaces i (pStoredItem)
            READ_STRUCT_PARAM pStoredItem STORED_ITEM_ID (iItemID)
            IF iItemID > -1 //item exists
                GET_LIST_STRING_VALUE_BY_INDEX lItemsNames iItemID tItemName
                READ_STRUCT_PARAM pStoredItem STORED_COUNT (iCount)
            ELSE
                tItemName = _______
                iCount = 0
            ENDIF
            WRITE_TO_FILE hFile 4 iCount
            WRITE_TO_FILE hFile 8 $tItemName
            ++i
        ENDWHILE

        CLOSE_FILE hFile
    ELSE
        PRINT_STRING_NOW "~r~Error: Can't create Inventory System save file." 5000
    ENDIF
    CLEO_RETURN 0 ()
}

{
    LVAR_INT hChar pInventorySpaces lItemsNames lItemsData lItemsScripts hObjectPicking //In
    LVAR_INT i hObject hClosestObject pClosestItemData iProgress pItemData iItemID pBuffer bCanReachObject
    LVAR_FLOAT charX charY charZ minX minY minZ x y z f fClosestDistance fScreenX fScreenY fLetterSizeX fLetterSizeY
    LVAR_TEXT_LABEL tItemName

    ProcessAllItemObjects:
    GET_CHAR_COORDINATES hChar (charX charY charZ)
    charZ -= 0.5

    fClosestDistance = 999999999.9
    i = 0
    WHILE GET_ANY_OBJECT_NO_SAVE_RECURSIVE iProgress (iProgress hObject)
        IF GET_EXTENDED_OBJECT_VAR hObject ISTM EXTENDED_VAR_ITEM_DATA pItemData

            // Update closest item object
            GET_OBJECT_COORDINATES hObject (x y z)
            GET_DISTANCE_BETWEEN_COORDS_3D charX charY charZ x y z (f)
            IF f < fClosestDistance
                fClosestDistance = f
                hClosestObject = hObject
                pClosestItemData = pItemData
            ENDIF

        ENDIF
    ENDWHILE

    // Process closest item object
    IF pClosestItemData > 0x0
        IF fClosestDistance < ITEM_OBJECT_MIN_DISTANCE_TO_SHOW

            bCanReachObject = TRUE
            GET_OBJECT_COORDINATES hClosestObject (x y z)
            
            IF NOT IS_LINE_OF_SIGHT_CLEAR x y z charX charY charZ 1 1 0 0 0
                z += 0.4
                charZ += 0.4
                IF NOT IS_LINE_OF_SIGHT_CLEAR x y z charX charY charZ 1 1 0 0 0
                    z -= 0.6
                    charZ -= 0.6
                    IF NOT IS_LINE_OF_SIGHT_CLEAR x y z charX charY charZ 1 1 0 0 0
                        bCanReachObject = FALSE
                    ENDIF
                ENDIF
            ENDIF

            IF bCanReachObject = TRUE
                GET_OBJECT_COORDINATES hClosestObject (x y z)

                READ_STRUCT_PARAM pClosestItemData DATA_ITEM_ID iItemID

                GET_LIST_STRING_VALUE_BY_INDEX lItemsNames iItemID tItemName

                z += ITEM_OBJECT_NAME_OFFSET
                CONVERT_3D_TO_SCREEN_2D x y z TRUE FALSE (fScreenX fScreenY fLetterSizeX fLetterSizeY)
                fLetterSizeX *= ITEM_OBJECT_TEXT_SIZE_MULT
                fLetterSizeY *= ITEM_OBJECT_TEXT_SIZE_MULT
                GET_FIXED_XY_ASPECT_RATIO fLetterSizeX fLetterSizeY (fLetterSizeX fLetterSizeY)

                GET_LABEL_POINTER Buffer pBuffer
                GET_TEXT_LABEL_STRING $tItemName (pBuffer)
                DRAW_STRING_EXT $pBuffer DRAW_EVENT_BEFORE_DRAWING fScreenX fScreenY fLetterSizeX fLetterSizeY FALSE FONT_SUBTITLES TRUE ALIGN_CENTER 640.0 FALSE (255 255 255 255) 1 0 (0 0 0 255) (0 0 0 0 0)

                IF fClosestDistance < ITEM_OBJECT_MIN_DISTANCE_TO_PICKUP
                    GET_OBJECT_COORDINATES hClosestObject (x y z)
                    z += ITEM_OBJECT_BUTTON_OFFSET
                    CONVERT_3D_TO_SCREEN_2D x y z TRUE FALSE (fScreenX fScreenY fLetterSizeX fLetterSizeY)
                    fLetterSizeX *= ITEM_OBJECT_TEXT_SIZE_MULT
                    fLetterSizeY *= ITEM_OBJECT_TEXT_SIZE_MULT
                    fLetterSizeX *= 0.8
                    fLetterSizeY *= 1.5
                    GET_FIXED_XY_ASPECT_RATIO fLetterSizeX fLetterSizeY (fLetterSizeX fLetterSizeY)

                    //GET_TEXT_LABEL_STRING INVSY01 (pBuffer)
                    //DRAW_STRING_EXT $pBuffer DRAW_EVENT_BEFORE_DRAWING fScreenX fScreenY fLetterSizeX fLetterSizeY FALSE FONT_SUBTITLES TRUE ALIGN_CENTER 640.0 FALSE (255 180 0 255) 1 0 (0 0 0 255) (0 0 0 0 0)
                    USE_TEXT_COMMANDS 1
                    SET_TEXT_FONT FONT_SUBTITLES
                    SET_TEXT_EDGE 1 0 0 0 255
                    SET_TEXT_COLOUR 255 180 0 255
                    SET_TEXT_CENTRE ON
                    SET_TEXT_SCALE fLetterSizeX fLetterSizeY
                    DISPLAY_TEXT fScreenX fScreenY INVSY01
                    USE_TEXT_COMMANDS 0

                    IF hObjectPicking = -1
                        IF IS_BUTTON_JUST_PRESSED PAD1 TRIANGLE
                        AND NOT IS_CHAR_DOING_ANY_IMPORTANT_TASK hChar INCLUDE_ANIMS_NONE
                            //CRIB CRIB_Use_Switch cima esquerda
                            //BD_FIRE wash_up algo embaixo
                            //BOMBER algo no chão
                            //GANGS DEALER_DEAL direita
                            //GANGS DRUGS_BUY esquerda
                            //MISC Case_pickup chão
                            //MISC GRAB_L pegando esquerda
                            //MISC GRAB_R pegando direita
                            //MISC PASS_Rifle_Ply pegando esquerda
                            //VENDING VEND_Use_pt2 baixo
                            
                            READ_STRUCT_PARAM pClosestItemData DATA_BONE i
                            IF i <= 24
                            OR i >= 26
                                TASK_PLAY_ANIM_SECONDARY hChar IS_PICK_L INVENTORYSYSTEM 3.0 0 0 0 0 -1
                                TASK_PLAY_ANIM_SECONDARY hChar IS_PICK_L INVENTORYSYSTEM 3.0 0 0 0 0 -1
                            ELSE
                                TASK_PLAY_ANIM_SECONDARY hChar IS_PICK_R INVENTORYSYSTEM 3.0 0 0 0 0 -1
                                TASK_PLAY_ANIM_SECONDARY hChar IS_PICK_R INVENTORYSYSTEM 3.0 0 0 0 0 -1
                            ENDIF
                            TASK_LOOK_AT_COORD hChar x y z 1000
                            GET_DISTANCE_BETWEEN_COORDS_2D x y charX charY (f)
                            IF f > 0.3
                                GET_ANGLE_FROM_TWO_COORDS charX charY x y (f)
                                SET_CHAR_HEADING hChar f
                            ENDIF
                            hObjectPicking = hClosestObject
                            IF GOSUB ProcessAllItemObjects_IsPickingObjectBellow
                                TASK_TOGGLE_DUCK hChar TRUE
                            ENDIF
                            //PRINT_STRING "start item pickup" 500
                        ENDIF
                    ELSE
                        IF DOES_OBJECT_EXIST hObjectPicking
                            IF IS_CHAR_PLAYING_ANIM hChar IS_PICK_L
                            OR IS_CHAR_PLAYING_ANIM hChar IS_PICK_R
                            OR IS_CHAR_DOING_ANY_IMPORTANT_TASK hChar INCLUDE_ANIMS_NONE
                                IF IS_CHAR_PLAYING_ANIM hChar IS_PICK_L
                                    GET_CHAR_ANIM_CURRENT_TIME hChar IS_PICK_L f
                                ELSE
                                    GET_CHAR_ANIM_CURRENT_TIME hChar IS_PICK_R f
                                ENDIF
                                IF f > 0.5
                                    hObjectPicking = hClosestObject
                                    IF GOSUB ProcessAllItemObjects_IsPickingObjectBellow
                                        TASK_TOGGLE_DUCK hChar FALSE
                                    ENDIF
                                    IF GET_EXTENDED_OBJECT_VAR hObjectPicking ISTM EXTENDED_VAR_ITEM_DATA pItemData
                                    AND pItemData > 0x0
                                        READ_STRUCT_PARAM pItemData DATA_ITEM_ID iItemID
                                        IF CLEO_CALL GiveItem 0 (hChar iItemID pInventorySpaces lItemsNames lItemsData lItemsScripts -1)(i)
                                            DELETE_OBJECT hObjectPicking
                                        ELSE
                                            PRINT_FORMATTED_NOW "ERROR: Can't give item, code %i" 3000 i
                                            WAIT 3000
                                        ENDIF
                                    ENDIF
                                ENDIF
                            ELSE
                                hObjectPicking = -1
                            ENDIF
                        ELSE
                            hObjectPicking = -1
                        ENDIF
                    ENDIF
                ENDIF
            ENDIF
        ENDIF
    ENDIF

    CLEO_RETURN 0 (hObjectPicking)

    ProcessAllItemObjects_IsPickingObjectBellow:
    GET_OBJECT_COORDINATES hObjectPicking x y z
    GET_CHAR_COORDINATES hChar charX charY charZ
    charZ -= z
    //PRINT_FORMATTED_NOW "%f" 1000 charZ
    IF charZ > 0.4
        RETURN_TRUE
    ELSE
        RETURN_FALSE
    ENDIF
    RETURN
}

{
    LVAR_INT iItemID lItemsScripts //In
    LVAR_INT i iUseMode
    LVAR_TEXT_LABEL tText

    IsAnyUseForItem:   
    iUseMode = USE_MAIN
    WHILE iUseMode <= USE_EXTERNAL_B
        CLEO_CALL GetUseConfigStringsIndex 0 (iItemID iUseMode)(i)
        i += SCRIPT_USE_SCRIPT
        GET_LIST_STRING_VALUE_BY_INDEX lItemsScripts i tText
        GET_STRING_LENGTH $tText (i)
        IF i > 0
            RETURN_TRUE
            CLEO_RETURN 0 ()
        ENDIF
        ++iUseMode
    ENDWHILE
    RETURN_FALSE
    CLEO_RETURN 0 ()
}

{
    LVAR_INT hChar pStoredItem iDecreaseCount lItemsScripts //In
    LVAR_INT i

    DecreaseItemCount:
    READ_STRUCT_PARAM pStoredItem STORED_COUNT (i)
    i -= iDecreaseCount
    IF i > 0
        WRITE_STRUCT_PARAM pStoredItem STORED_COUNT i
        RETURN_TRUE
        CLEO_RETURN 0 ()
    ELSE
        CLEO_CALL ClearStoredItem 0 (hChar pStoredItem lItemsScripts)()
    ENDIF
    RETURN_FALSE
    CLEO_RETURN 0 ()
}

{
    LVAR_INT hChar pStoredItem iCurrentUseMode lItemsScripts iEventID //In
    LVAR_INT i iUseScriptIndex iUseValuesIndex iStringsIndex iItemID pBuffer pBuffer2 argsPointer[8] 
    LVAR_FLOAT args[8]
    LVAR_TEXT_LABEL16 tText

    StartUseOrEvent:
    READ_STRUCT_PARAM pStoredItem STORED_ITEM_ID (iItemID)

    IF iEventID = -1
        CLEO_CALL GetUseConfigStringsIndex 0 (iItemID iCurrentUseMode)(iStringsIndex)
    ELSE
        i = USE_EVENT
        CLEO_CALL GetUseConfigStringsIndex 0 (iItemID i)(iStringsIndex)
    ENDIF

    iUseScriptIndex = iStringsIndex + SCRIPT_USE_SCRIPT

    GET_LIST_STRING_VALUE_BY_INDEX lItemsScripts iUseScriptIndex tText
    
    GET_STRING_LENGTH $tText (i)
    IF i > 0
        GET_LABEL_POINTER Buffer (pBuffer)
        IF iEventID = -1
            STRING_FORMAT pBuffer "IS Scripts\Use\%s.cs" $tText
        ELSE
            STRING_FORMAT pBuffer "IS Scripts\Event\%s.cs" $tText
        ENDIF

        iUseValuesIndex = iStringsIndex + SCRIPT_USE_VALUES
        GET_LIST_STRING_VALUE_BY_INDEX lItemsScripts iUseValuesIndex tText
        GET_LABEL_POINTER Buffer2 (pBuffer2)
        //COPY_STRING "%f%f" pBuffer
        STRING_FORMAT pBuffer2 "%s" $tText
        //SCAN_STRING $tText "%s %s %s %s %s %s %s %s" (j) args[0] args[1] args[2] args[3] args[4] args[5] args[6] args[7]

        i = 0
        WHILE i < 8
            GET_VAR_POINTER args[i] argsPointer[i]
            ++i
        ENDWHILE

        //sscanf because SCAN_STRING doesn't work on current gta3script version
        CALL_FUNCTION_RETURN 0x8220AD 10 10 (argsPointer[7] argsPointer[6] argsPointer[5] argsPointer[4] argsPointer[3] argsPointer[2] argsPointer[1] argsPointer[0], "%f%f%f%f%f%f%f%f" pBuffer2)(i)

        //PRINT_FORMATTED_NOW "%i %f %f %f" 10000 j args[0] args[1] args[2]

        IF iEventID > -1 // is event
            IF STREAM_CUSTOM_SCRIPT $pBuffer (hChar pStoredItem iEventID args[0] args[1] args[2] args[3] args[4] args[5] args[6] args[7])
                RETURN_TRUE
                CLEO_RETURN 0 ()
            ELSE
                PRINT_FORMATTED_NOW "~r~Can't start script '%s'" 5000 $pBuffer
            ENDIF
        ELSE
            IF STREAM_CUSTOM_SCRIPT $pBuffer (hChar pStoredItem args[0] args[1] args[2] args[3] args[4] args[5] args[6] args[7])
                READ_STRUCT_PARAM pStoredItem STORED_FLAGS i
                SET_LOCAL_VAR_BIT_CONST i ITEM_FLAG_USING
                WRITE_STRUCT_PARAM pStoredItem STORED_FLAGS i
                RETURN_TRUE
                CLEO_RETURN 0 ()
            ELSE
                PRINT_FORMATTED_NOW "~r~Can't start script '%s'" 5000 $pBuffer
            ENDIF
        ENDIF
    ENDIF

    RETURN_FALSE
    CLEO_RETURN 0 ()
}

{
    LVAR_INT iItemID iCurrentUseMode //In
    LVAR_INT i

    GetUseConfigStringsIndex:

    i = iItemID * SCRIPT_TOTAL_USE_STRINGS_EACH_ITEM

    SWITCH iCurrentUseMode
        CASE USE_MAIN
            i += SCRIPT_USE_MAIN_STRING_INDEX
            BREAK
        CASE USE_POSITION
            i += SCRIPT_USE_POS_STRING_INDEX
            BREAK
        CASE USE_EXTERNAL_A
            i += SCRIPT_USE_A_STRING_INDEX
            BREAK
        CASE USE_EXTERNAL_B
            i += SCRIPT_USE_B_STRING_INDEX
            BREAK
        CASE USE_EVENT
            i += SCRIPT_USE_EVENT_STRING_INDEX
            BREAK
    ENDSWITCH

    CLEO_RETURN 0 (i)
}

{
    LVAR_INT hChar pItemData //In
    LVAR_INT bCanHoldItem pWeaponInfo iFlags iBone

    CanHoldItem:
    bCanHoldItem = TRUE
    GET_CURRENT_CHAR_WEAPONINFO hChar pWeaponInfo
    IF pWeaponInfo > 0x0
        GET_WEAPONINFO_FLAGS pWeaponInfo iFlags
        /*
            unsigned int bCanAim : 1;0
            unsigned int bAimWithArm : 1;1
            unsigned int b1stPerson : 1;2
            unsigned int bOnlyFreeAim : 1;3
            unsigned int bMoveAim : 1;4 // can move when aiming
            unsigned int bMoveFire : 1;5 // can move when firing
            unsigned int b06 : 1;6 // this bitfield is not used
            unsigned int b07 : 1;7 // this bitfield is not used
            unsigned int bThrow : 1;8
            unsigned int bHeavy : 1;9 // can't run fast with this weapon
            unsigned int bContinuosFire :10 1;
            unsigned int bTwinPistol :11 1;
            unsigned int bReload : 1;12 // this weapon can be reloaded
            unsigned int bCrouchFire : 1;13 // can reload when crouching
            unsigned int bReload2Start : 1;14 // reload directly after firing
            unsigned int bLongReload : 1;15
            unsigned int bSlowdown : 1;16
            unsigned int bRandSpeed : 1;17
            unsigned int bExpands : 1;18
        */
        IF IS_LOCAL_VAR_BIT_SET_CONST iFlags 11 //bTwinPistol
        OR IS_LOCAL_VAR_BIT_SET_CONST iFlags 9 //bHeavy
            bCanHoldItem = FALSE
        ELSE
            IF pItemData > 0x0
                READ_STRUCT_PARAM pItemData DATA_BONE iBone
                IF iBone <= 34
                OR iBone >= 36
                    bCanHoldItem = FALSE
                ENDIF
            ELSE
                bCanHoldItem = FALSE
            ENDIF
        ENDIF
    ENDIF
    CLEO_RETURN 0 (bCanHoldItem)
}

{
    LVAR_INT pSelectedStoredItem hRenderObjectItem //In
    LVAR_INT scplayer iItemModel iSelectedStoredItemData iBone
    LVAR_FLOAT x y z rX rY rZ

    SelectItem:
    IF NOT hRenderObjectItem = 0
        DELETE_RENDER_OBJECT hRenderObjectItem
        hRenderObjectItem = 0
    ENDIF
    GET_PLAYER_CHAR 0 scplayer
    READ_STRUCT_PARAM pSelectedStoredItem STORED_ITEM_DATA iSelectedStoredItemData
    READ_STRUCT_PARAM iSelectedStoredItemData DATA_MODEL_ID (iItemModel)
    IF iItemModel > 0
        READ_STRUCT_PARAM iSelectedStoredItemData DATA_BONE (iBone)
        IF iBone > -1
            READ_STRUCT_PARAM iSelectedStoredItemData DATA_OFFSET_X (x)
            READ_STRUCT_PARAM iSelectedStoredItemData DATA_OFFSET_Y (y)
            READ_STRUCT_PARAM iSelectedStoredItemData DATA_OFFSET_Z (z)
            READ_STRUCT_PARAM iSelectedStoredItemData DATA_ROTATION_X (rX)
            READ_STRUCT_PARAM iSelectedStoredItemData DATA_ROTATION_Y (rY)
            READ_STRUCT_PARAM iSelectedStoredItemData DATA_ROTATION_Z (rZ)
            REQUEST_MODEL iItemModel
            LOAD_ALL_MODELS_NOW
            CREATE_RENDER_OBJECT_TO_CHAR_BONE scplayer iItemModel iBone (x y z)(rX rY rZ) hRenderObjectItem
            MARK_MODEL_AS_NO_LONGER_NEEDED iItemModel
        ENDIF
    ENDIF
    CLEO_RETURN 0 (hRenderObjectItem)
}

{
    LVAR_INT iMode pInventorySpaces lItemsNames pDragStoredItem iSelectedSlot hChar lItemsScripts //In
    LVAR_INT iRow i j pStoredItem iStoredItemID pBuffer
    LVAR_FLOAT x y f xOffset yOffset xSize ySize fRowSize fColSize fItemSizeX fItemSizeY

    ProcessInventory:

    IF iMode = 1
        DRAW_TEXTURE_PLUS 0 DRAW_EVENT_AFTER_DRAWING INVENTORY_WINDOW_BACK_POS_X INVENTORY_WINDOW_BACK_POS_Y (INVENTORY_WINDOW_BACK_SIZE_X INVENTORY_WINDOW_BACK_SIZE_Y) 0.0 0.0 TRUE 0 0 (0 0 0 100)
    ENDIF

    GET_FIXED_XY_ASPECT_RATIO INVENTORY_WINDOW_COL_SIZE INVENTORY_WINDOW_ROW_SIZE (fColSize fRowSize)
    GET_FIXED_XY_ASPECT_RATIO INVENTORY_WINDOW_ITEM_SIZE INVENTORY_WINDOW_ITEM_SIZE (fItemSizeX fItemSizeY)

    yOffset = fRowSize // start from last row

    iRow = 0
    i = 0
    WHILE i < INVENTORY_TOTAL_SPACES

        MOD i INVENTORY_TOTAL_COL (j)
        IF j = 0
            xOffset = 0.0
            IF iRow = 1
                IF iMode = 2
                    BREAK
                ENDIF
                yOffset = 0.0
            ELSE
                yOffset += fRowSize
            ENDIF
            ++iRow
        ENDIF

        x = INVENTORY_WINDOW_POS_X + xOffset
        xOffset += fColSize

        y = INVENTORY_WINDOW_POS_Y + yOffset

        IF iSelectedSlot = i
            GET_FIXED_XY_ASPECT_RATIO INVENTORY_WINDOW_ITEM_SIZE_SELECTED INVENTORY_WINDOW_ITEM_SIZE_SELECTED (xSize ySize)
            DRAW_TEXTURE_PLUS 0 DRAW_EVENT_AFTER_DRAWING x y (xSize ySize) 0.0 0.0 FALSE 0 0 (40 140 0 255)
        ENDIF

        DRAW_TEXTURE_PLUS 0 DRAW_EVENT_AFTER_DRAWING x y (INVENTORY_WINDOW_ITEM_SIZE INVENTORY_WINDOW_ITEM_SIZE) 0.0 0.0 TRUE 0 0 (0 0 0 120)


        READ_STRUCT_PARAM pInventorySpaces i (pStoredItem)
        READ_STRUCT_PARAM pStoredItem STORED_ITEM_ID (iStoredItemID)
        //PRINT_FORMATTED_NOW "show stored %x %i" 1000 pStoredItem i
        //WAIT 200
        IF iStoredItemID > -1 //has item
            
            STREAM_CUSTOM_SCRIPT_FROM_LABEL ScriptDrawItemIcon (lItemsNames pStoredItem 0 x y 1.0)
            //DRAW_TEXTURE_PLUS 0 DRAW_EVENT_AFTER_HUD x y (INVENTORY_WINDOW_ITEM_SIZE INVENTORY_WINDOW_ITEM_SIZE) 0.0 0.0 TRUE 0 0 (255 0 0 180)

        ELSE

            IF iRow = 1
                GET_LABEL_POINTER ReallyTempBuffer (pBuffer)
                j = i + 1
                STRING_FORMAT pBuffer "%i" j

                xSize = 0.4
                ySize = 0.8

                f = fItemSizeY * -0.25
                f += y
                DRAW_STRING_EXT $pBuffer DRAW_EVENT_BEFORE_HUD x f xSize ySize TRUE FONT_SUBTITLES TRUE ALIGN_CENTER 640.0 FALSE (255 255 255 50) 0 0 (0 0 0 0) FALSE (0 0 0 0)
            ENDIF

        ENDIF

        IF iMode = 1
            f = INVENTORY_WINDOW_ITEM_SIZE
            IF CLEO_CALL IsCursorInsideBox 0 (x y f f 1)
                IF IS_KEY_PRESSED VK_LBUTTON
                OR IS_KEY_PRESSED VK_RBUTTON
                    DRAW_TEXTURE_PLUS 0 DRAW_EVENT_AFTER_DRAWING x y (f f) 0.0 0.0 TRUE 0 0 (60 60 60 160)
                    IF IS_KEY_JUST_PRESSED VK_LBUTTON
                    OR IS_KEY_JUST_PRESSED VK_RBUTTON
                        CLEO_CALL InventorySpaceClick 0 (hChar pDragStoredItem pStoredItem lItemsScripts)()
                    ENDIF
                ELSE
                    DRAW_TEXTURE_PLUS 0 DRAW_EVENT_AFTER_DRAWING x y (f f) 0.0 0.0 TRUE 0 0 (80 80 80 140)
                ENDIF
            ENDIF
        ENDIF

        ++i
    ENDWHILE

    IF iMode = 1
        CLEO_CALL CursorProcess 0 ()
        CLEO_CALL DrawDragItemIntoCursor 0 (pDragStoredItem lItemsNames)
    ENDIF

    CLEO_RETURN 0 ()
}

{
    LVAR_INT hChar pDragStoredItem pClickStoredItem lItemsScripts //In
    LVAR_INT i pStoredItem bDrag iRestCount bIsOdd iDivRest

    InventorySpaceClick:

    READ_STRUCT_PARAM pDragStoredItem STORED_ITEM_ID (i)
    IF i > -1 //has item
        // put drag icon into space
        CLEO_CALL DragItemToItem 0 (hChar pDragStoredItem pClickStoredItem lItemsScripts)(i)
    ELSE
        // clicking space without drag
        READ_STRUCT_PARAM pClickStoredItem STORED_ITEM_ID (i)
        IF i > -1 // has item
            // start drag
            IF IS_KEY_JUST_PRESSED VK_RBUTTON //half
                READ_STRUCT_PARAM pClickStoredItem STORED_COUNT i
                IF i > 1
                    CLEO_CALL CopyStoredItemFromTo 0 (pClickStoredItem pDragStoredItem)()
                    MOD i 2 (iDivRest)
                    i /= 2
                    WRITE_STRUCT_PARAM pDragStoredItem STORED_COUNT i
                    i += iDivRest
                    WRITE_STRUCT_PARAM pClickStoredItem STORED_COUNT i
                ENDIF
            ELSE
                CLEO_CALL CopyStoredItemFromTo 0 (pClickStoredItem pDragStoredItem)()
                CLEO_CALL ClearStoredItem 0 (hChar pClickStoredItem lItemsScripts)()
            ENDIF
        ENDIF
    ENDIF

    CLEO_RETURN 0 ()
}

{
    LVAR_INT pStoredItemFrom pStoredItemTo //In
    LVAR_INT i
    LVAR_FLOAT f

    CopyStoredItemFromTo:
    READ_STRUCT_PARAM pStoredItemFrom STORED_ITEM_ID i
    WRITE_STRUCT_PARAM pStoredItemTo STORED_ITEM_ID i

    READ_STRUCT_PARAM pStoredItemFrom STORED_ITEM_DATA i
    WRITE_STRUCT_PARAM pStoredItemTo STORED_ITEM_DATA i

    READ_STRUCT_PARAM pStoredItemFrom STORED_COUNT i
    WRITE_STRUCT_PARAM pStoredItemTo STORED_COUNT i

    READ_STRUCT_PARAM pStoredItemFrom STORED_FLAGS i
    WRITE_STRUCT_PARAM pStoredItemTo STORED_FLAGS i

    CLEO_RETURN 0
}

{
    LVAR_INT pStoredItemFrom pStoredItemTo //In
    LVAR_INT iFrom iTo
    LVAR_FLOAT f

    SwapStoredItemFromTo:
    READ_STRUCT_PARAM pStoredItemFrom STORED_ITEM_ID iFrom
    READ_STRUCT_PARAM pStoredItemTo STORED_ITEM_ID iTo
    WRITE_STRUCT_PARAM pStoredItemFrom STORED_ITEM_ID iTo
    WRITE_STRUCT_PARAM pStoredItemTo STORED_ITEM_ID iFrom

    READ_STRUCT_PARAM pStoredItemFrom STORED_ITEM_DATA iFrom
    READ_STRUCT_PARAM pStoredItemTo STORED_ITEM_DATA iTo
    WRITE_STRUCT_PARAM pStoredItemFrom STORED_ITEM_DATA iTo
    WRITE_STRUCT_PARAM pStoredItemTo STORED_ITEM_DATA iFrom

    READ_STRUCT_PARAM pStoredItemFrom STORED_COUNT iFrom
    READ_STRUCT_PARAM pStoredItemTo STORED_COUNT iTo
    WRITE_STRUCT_PARAM pStoredItemFrom STORED_COUNT iTo
    WRITE_STRUCT_PARAM pStoredItemTo STORED_COUNT iFrom

    READ_STRUCT_PARAM pStoredItemFrom STORED_FLAGS iFrom
    READ_STRUCT_PARAM pStoredItemTo STORED_FLAGS iTo
    WRITE_STRUCT_PARAM pStoredItemFrom STORED_FLAGS iTo
    WRITE_STRUCT_PARAM pStoredItemTo STORED_FLAGS iFrom

    CLEO_RETURN 0
}

{
    LVAR_INT hChar pStoredItem lItemsScripts //In
    LVAR_INT i

    ClearStoredItem:
    i = EVENT_ITEM_BEFORE_CLEAR
    CLEO_CALL StartUseOrEvent 0 (hChar pStoredItem 0 lItemsScripts i)()
    CLEO_CALL InitEmptyStoredItem 0 (pStoredItem)
    CLEO_RETURN 0 
}

{
    LVAR_INT hChar pStoredItemFrom pStoredItemTo lItemsScripts //In
    LVAR_INT iRestCount pStoredItemDataFrom pStoredItemDataTo iStoredItemIDFrom iStoredItemIDTo iMaxStack iStoredCountFrom iStoredCountTo
    // Returns: INT for mode

    DragItemToItem:
    IF pStoredItemFrom = pStoredItemTo
        CLEO_RETURN 0 (0)
    ENDIF

    READ_STRUCT_PARAM pStoredItemFrom DATA_ITEM_ID iStoredItemIDFrom
    READ_STRUCT_PARAM pStoredItemTo DATA_ITEM_ID iStoredItemIDTo

    IF iStoredItemIDFrom < 0
        CLEO_RETURN 0 (1)
    ENDIF

    IF iStoredItemIDFrom = iStoredItemIDTo
        READ_STRUCT_PARAM pStoredItemTo STORED_ITEM_DATA pStoredItemDataTo
        READ_STRUCT_PARAM pStoredItemDataTo DATA_STACK iMaxStack
        IF iMaxStack > 1 //is stackable
            READ_STRUCT_PARAM pStoredItemFrom STORED_COUNT iStoredCountFrom
            READ_STRUCT_PARAM pStoredItemTo STORED_COUNT iStoredCountTo
            iStoredCountTo += iStoredCountFrom
            iRestCount = iStoredCountTo - iMaxStack
            IF iRestCount < 0
                iRestCount = 0
            ENDIF
            WRITE_STRUCT_PARAM pStoredItemFrom STORED_COUNT iRestCount
            iStoredCountTo -= iRestCount
            WRITE_STRUCT_PARAM pStoredItemTo STORED_COUNT iStoredCountTo
        ENDIF
    ELSE
        IF iStoredItemIDTo > -1 //has item
            //swap drag item
            CLEO_CALL SwapStoredItemFromTo 0 (pStoredItemFrom pStoredItemTo)
            CLEO_RETURN 0 (3)
        ELSE
            //put drag item on empty space
            CLEO_CALL CopyStoredItemFromTo 0 (pStoredItemFrom pStoredItemTo)
            CLEO_CALL ClearStoredItem 0 (hChar pStoredItemFrom lItemsScripts)
            CLEO_RETURN 0 (5)
        ENDIF
    ENDIF

    IF iRestCount < 1
        WRITE_STRUCT_PARAM pStoredItemFrom STORED_ITEM_ID -1 // no drag
        CLEO_RETURN 0 (2)
    ELSE
        //swap drag item
        //CLEO_CALL SwapStoredItemFromTo 0 (pStoredItemFrom pStoredItemTo)
        CLEO_RETURN 0 (6)
    ENDIF
    CLEO_RETURN 0 (4)
}

{
    LVAR_FLOAT x y //In
    LVAR_INT pCursorPos

    CursorInit:
    GET_LABEL_POINTER CursorPos pCursorPos
    WRITE_STRUCT_PARAM pCursorPos 0 x
    WRITE_STRUCT_PARAM pCursorPos 1 y
    CLEO_RETURN 0
}

{
    LVAR_FLOAT x y sizeX sizeY //In
    LVAR_INT bFixedAspectRatio //In
    LVAR_FLOAT posX posY f minX maxX minY maxY
    LVAR_INT pCursorPos

    IsCursorInsideBox:
    GET_LABEL_POINTER CursorPos pCursorPos
    READ_STRUCT_PARAM pCursorPos 0 (posX)
    READ_STRUCT_PARAM pCursorPos 1 (posY)

    IF bFixedAspectRatio = TRUE
        GET_FIXED_XY_ASPECT_RATIO sizeX sizeY (sizeX sizeY)
    ENDIF

    f = sizeX * 0.5
    minX = x - f
    maxX = x + f
    IF posX < minX
    OR posX > maxX
        RETURN_FALSE
        CLEO_RETURN 0
    ENDIF

    f = sizeY * 0.5
    minY = y - f
    maxY = y + f
    IF posY < minY
    OR posY > maxY
        RETURN_FALSE
        CLEO_RETURN 0
    ENDIF

    RETURN_TRUE
    CLEO_RETURN 0
}

{
    LVAR_INT i pCursorTexture pCursorPos
    LVAR_FLOAT f posX posY moveX moveY offsetX offsetY

    CursorProcess:

    GET_LABEL_POINTER CursorPos pCursorPos
    READ_STRUCT_PARAM pCursorPos 0 (posX)
    READ_STRUCT_PARAM pCursorPos 1 (posY)

    GET_PC_MOUSE_MOVEMENT moveX moveY

    GET_FIXED_XY_ASPECT_RATIO moveX moveY (moveX moveY)

    IF IS_MOUSE_USING_VERTICAL_INVERSION
        moveY *= -1.0
    ENDIF

    // Process acceleration
    f = moveX
    moveX *= moveX
    IF f < 0.0
        moveX *= -1.0
    ENDIF
    f *= CURSOR_NON_ACCELERATION
    moveX += f

    f = moveY
    moveY *= moveY
    IF f < 0.0
        moveY *= -1.0
    ENDIF
    f *= CURSOR_NON_ACCELERATION
    moveY += f
    
    // Set pos
    GET_MOUSE_SENSIBILITY (f)
    f *= CURSOR_SENSIBILITY
    moveX *= f
    moveY *= f
    posX += moveX
    posY += moveY
    CLAMP_FLOAT posX 0.0 639.0 (posX)
    CLAMP_FLOAT posY 0.0 447.0 (posY)

    WRITE_STRUCT_PARAM pCursorPos 0 posX
    WRITE_STRUCT_PARAM pCursorPos 1 posY

    // Draw
    GET_FIXED_XY_ASPECT_RATIO CURSOR_SIZE_X CURSOR_SIZE_Y (offsetX offsetY)
    offsetX *= 0.5
    offsetY *= 0.5
    posX -= offsetX
    posY += offsetY
    GET_TEXTURE_FROM_SPRITE 1 (pCursorTexture)
    DRAW_TEXTURE_PLUS pCursorTexture DRAW_EVENT_AFTER_FADE posX posY CURSOR_SIZE_X CURSOR_SIZE_Y 0.0 0.0 TRUE 0 0 (255 255 255 255)
    CLEO_RETURN 0

    CursorPos:
    DUMP
    00 00 00 00 //pos X
    00 00 00 00 //pos Y
    ENDDUMP
}

{
    LVAR_INT pDragStoredItem lItemsNames //In
    LVAR_INT pCursorPos pTexture i
    LVAR_FLOAT posX posY offsetX offsetY

    DrawDragItemIntoCursor:
    READ_STRUCT_PARAM pDragStoredItem STORED_ITEM_ID (i)
    IF i > -1
        GET_LABEL_POINTER CursorPos pCursorPos
        READ_STRUCT_PARAM pCursorPos 0 (posX)
        READ_STRUCT_PARAM pCursorPos 1 (posY)

        GET_FIXED_XY_ASPECT_RATIO CURSOR_SIZE_X CURSOR_SIZE_Y (offsetX offsetY)
        offsetX *= 0.6
        offsetY *= 0.4
        posX -= offsetX
        posY -= offsetY
        STREAM_CUSTOM_SCRIPT_FROM_LABEL ScriptDrawItemIcon (lItemsNames pDragStoredItem 1 posX posY 0.6)
        //GET_TEXTURE_FROM_SPRITE 1 (pTexture)
        //DRAW_TEXTURE_PLUS pTexture DRAW_EVENT_AFTER_FADE posX posY CURSOR_SIZE_X CURSOR_SIZE_Y 0.0 0.0 TRUE 0 0 (255 255 255 255)
    ENDIF
    CLEO_RETURN 0
}

{
    LVAR_INT lItemsNames pStoredItem bPriority //In
    LVAR_FLOAT x y sizeMult //In
    LVAR_FLOAT sizeX sizeY
    LVAR_INT i pBuffer
    LVAR_TEXT_LABEL tThisItemName

    ScriptDrawItemIcon:
    READ_STRUCT_PARAM pStoredItem STORED_ITEM_ID (i)
    IF i > -1 //has item
        GET_LIST_STRING_VALUE_BY_INDEX lItemsNames i (tThisItemName)
        LOAD_TEXTURE_DICTIONARY $tThisItemName
        LOAD_SPRITE 1 "icon"
        GET_TEXTURE_FROM_SPRITE 1 (i)

        sizeX = INVENTORY_WINDOW_ITEM_SIZE_INVERTED * sizeMult
        sizeY = INVENTORY_WINDOW_ITEM_SIZE * sizeMult

        IF bPriority = TRUE
            DRAW_TEXTURE_PLUS i DRAW_EVENT_AFTER_HUD x y (sizeX sizeY) 0.0 0.0 TRUE 0 0 (255 255 255 255)
        ELSE
            DRAW_TEXTURE_PLUS i DRAW_EVENT_BEFORE_HUD x y (sizeX sizeY) 0.0 0.0 TRUE 0 0 (255 255 255 255)
        ENDIF
        
        READ_STRUCT_PARAM pStoredItem STORED_COUNT (i)

        GET_LABEL_POINTER ReallyTempBuffer (pBuffer)
        STRING_FORMAT pBuffer "%i" i

        sizeX = 0.35 * sizeMult
        sizeY = 0.7 * sizeMult

        IF bPriority = TRUE
            DRAW_STRING $pBuffer DRAW_EVENT_AFTER_HUD x y sizeX sizeY TRUE FONT_SUBTITLES
        ELSE
            DRAW_STRING $pBuffer DRAW_EVENT_BEFORE_HUD x y sizeX sizeY TRUE FONT_SUBTITLES
        ENDIF
        REMOVE_TEXTURE_DICTIONARY
    ENDIF
    TERMINATE_THIS_CUSTOM_SCRIPT
}

{
    LVAR_INT iUseMode // In
    LVAR_FLOAT x y // In
    LVAR_INT iItemID lItemsNames lItemsScripts //In
    LVAR_INT i iStringsIndex iUseIconIndex pTexture iSpriteID
    LVAR_FLOAT f
    LVAR_TEXT_LABEL16 tText

    DrawUseIcon:
    CLEO_CALL GetUseConfigStringsIndex 0 (iItemID iUseMode)(iStringsIndex)
    iUseIconIndex = iStringsIndex + SCRIPT_USE_ICON

    GET_LIST_STRING_VALUE_BY_INDEX lItemsScripts iUseIconIndex tText

    GET_STRING_LENGTH $tText (i)
    IF i = 1
        GET_VAR_POINTER $tText (i)
        READ_MEMORY i 1 FALSE (i)
        i -= 49
        IF i >= 0
        AND i < 9
            iSpriteID = i + SPRITE_USE_START_ID
        ENDIF
    ENDIF

    IF iSpriteID > 0 // use root script sprite ID
        GET_TEXTURE_FROM_SPRITE iSpriteID pTexture
        IF pTexture > 0x0
            DRAW_TEXTURE_PLUS pTexture DRAW_EVENT_AFTER_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (255 255 255 255)
        ENDIF
    ELSE
        //
        iSpriteID = iUseMode + 1
        STREAM_CUSTOM_SCRIPT_FROM_LABEL ScriptDrawUseIcon iItemID iUseIconIndex lItemsNames lItemsScripts iSpriteID x y
    ENDIF

    CLEO_RETURN 0 ()
}

{
    LVAR_INT iItemID iUseIconIndex lItemsNames lItemsScripts iSpriteID //In
    LVAR_FLOAT x y //In
    LVAR_INT pTexture
    LVAR_TEXT_LABEL16 tIconName
    LVAR_TEXT_LABEL tTxdName

    ScriptDrawUseIcon:
    GET_LIST_STRING_VALUE_BY_INDEX lItemsScripts iUseIconIndex tIconName
    GET_LIST_STRING_VALUE_BY_INDEX lItemsNames iItemID tTxdName
    LOAD_TEXTURE_DICTIONARY $tTxdName
    LOAD_SPRITE iSpriteID $tIconName
    GET_TEXTURE_FROM_SPRITE iSpriteID pTexture
    IF pTexture > 0x0
        DRAW_TEXTURE_PLUS pTexture DRAW_EVENT_AFTER_HUD x y SELECT_USE_ITEM_SIZE SELECT_USE_ITEM_SIZE 0.0 0.0 TRUE 0 0 (255 255 255 255)
    ENDIF
    REMOVE_TEXTURE_DICTIONARY
    TERMINATE_THIS_CUSTOM_SCRIPT
}

{
    LVAR_INT hChar pGiveItemName pInventorySpaces lItemsNames lItemsData lItemsScripts pStoredItemDest //In
    LVAR_INT i iErrorCode pItemData pStoredItem bNew

    GiveItem:
    // TODO support variable quantity

    //PRINT_FORMATTED_NOW "trying give '%s'" 2000 $pGiveItemName
    //WAIT 2000

    IF pGiveItemName > 0xFFFF // is item name
        IF CLEO_CALL FindItemDataByName 0 (pGiveItemName lItemsNames lItemsData)(pItemData)
        ELSE
            iErrorCode = 1
        ENDIF
    ELSE
        GET_LIST_VALUE_BY_INDEX lItemsData pGiveItemName (pItemData)
    ENDIF

    IF pItemData > 0x0
        READ_STRUCT_PARAM pItemData DATA_STACK (i)

        IF pStoredItemDest = -1
            IF i > 0 // is stackable
                IF CLEO_CALL FindStoredItemByData 0 (pInventorySpaces pItemData)(pStoredItem)
                ELSE
                    CLEO_CALL FindEmptyInventorySpace 0 (pInventorySpaces)(pStoredItem)
                ENDIF
            ELSE
                CLEO_CALL FindEmptyInventorySpace 0 (pInventorySpaces)(pStoredItem)
            ENDIF
        ELSE
            pStoredItem = pStoredItemDest
        ENDIF

        IF NOT pStoredItem = -1
            IF CLEO_CALL SetStoredItemData 0 (pStoredItem pItemData)(bNew)
                iErrorCode = 0
                IF bNew = TRUE
                    i = EVENT_ITEM_AFTER_GIVE
                ELSE
                    i = EVENT_ITEM_AFTER_INCREASE_COUNT
                ENDIF
                CLEO_CALL StartUseOrEvent 0 (hChar pStoredItem 0 lItemsScripts i)()
                RETURN_TRUE
                CLEO_RETURN 0 (iErrorCode)
            ELSE
                iErrorCode = 3
            ENDIF
        ELSE
            iErrorCode = 2
        ENDIF
    ENDIF

    RETURN_FALSE
    CLEO_RETURN 0 (iErrorCode)
}

{
    LVAR_INT pStoredItem pItemData //In
    LVAR_INT iDataID iStoredID iMaxStack i

    SetStoredItemData:
    READ_STRUCT_PARAM pItemData DATA_STACK (iMaxStack)
    READ_STRUCT_PARAM pItemData DATA_ITEM_ID (iDataID)
    IF iMaxStack > 0 //is stackable
        READ_STRUCT_PARAM pStoredItem STORED_ITEM_ID iStoredID
        IF iDataID = iStoredID
            READ_STRUCT_PARAM pStoredItem STORED_COUNT i
            IF i < iMaxStack
                ++i
                WRITE_STRUCT_PARAM pStoredItem STORED_COUNT i
                RETURN_TRUE
                CLEO_RETURN 0 (0)
            ELSE
                RETURN_FALSE
                CLEO_RETURN 0 (0)
            ENDIF
        ENDIF
    ENDIF
    // Item is new
    WRITE_STRUCT_PARAM pStoredItem STORED_ITEM_ID iDataID
    WRITE_STRUCT_PARAM pStoredItem STORED_ITEM_DATA pItemData
    WRITE_STRUCT_PARAM pStoredItem STORED_COUNT 1
    WRITE_STRUCT_PARAM pStoredItem STORED_FLAGS 0
    RETURN_TRUE
    CLEO_RETURN 0 (1)
}

{
    LVAR_INT pInventorySpaces pItemData //In
    LVAR_INT i j iMaxStack pStoredItem iItemDataID

    FindStoredItemByData:
    READ_STRUCT_PARAM pItemData DATA_ITEM_ID (iItemDataID)
    READ_STRUCT_PARAM pItemData DATA_STACK (iMaxStack)
    i = 0
    WHILE i < INVENTORY_TOTAL_SPACES
        READ_STRUCT_PARAM pInventorySpaces i (pStoredItem)
        READ_STRUCT_PARAM pStoredItem STORED_ITEM_ID (j)
        IF j = iItemDataID
            READ_STRUCT_PARAM pStoredItem STORED_COUNT (j)
            IF j < iMaxStack
                RETURN_TRUE
                CLEO_RETURN 0 (pStoredItem)
            ENDIF
        ENDIF
        ++i
    ENDWHILE
    RETURN_FALSE
    CLEO_RETURN 0 (-1)
}

{
    LVAR_INT pInventorySpaces //In
    LVAR_INT i j pStoredItem iItemDataID

    FindEmptyInventorySpace:
    i = 0
    WHILE i < INVENTORY_TOTAL_SPACES
        READ_STRUCT_PARAM pInventorySpaces i (pStoredItem)
        READ_STRUCT_PARAM pStoredItem STORED_ITEM_ID (j)
        IF j = -1 //no item
            RETURN_TRUE
            CLEO_RETURN 0 (pStoredItem)
        ENDIF
        ++i
    ENDWHILE
    RETURN_FALSE
    CLEO_RETURN 0 (-1)
}

{
    LVAR_INT pInventorySpaces //Int
    LVAR_INT iOffset iSpaceCount iTotalSpace iSizeOfEachStoredItem pStoredItemsArray pStoredItem

    InitInventory:
    // instead of allocate each space, allocate all once and split to each pointer, avoiding memory fragmentation
    iSizeOfEachStoredItem = STORED_TOTAL_PARAMS * 4
    iTotalSpace = INVENTORY_TOTAL_SPACES * iSizeOfEachStoredItem
    ALLOCATE_MEMORY iTotalSpace (pStoredItemsArray)
    WRITE_MEMORY pStoredItemsArray iTotalSpace 0x00 FALSE //clear bytes
    iOffset = 0
    iSpaceCount = 0
    WHILE iOffset < iTotalSpace
        pStoredItem = pStoredItemsArray + iOffset
        WRITE_STRUCT_PARAM pInventorySpaces iSpaceCount pStoredItem
        CLEO_CALL InitEmptyStoredItem 0 (pStoredItem)
        iOffset += iSizeOfEachStoredItem
        ++iSpaceCount
    ENDWHILE
    CLEO_RETURN 0 ()
}

{
    LVAR_INT pStoredItem //In
    InitEmptyStoredItem:
    WRITE_STRUCT_PARAM pStoredItem STORED_ITEM_ID -1
    WRITE_STRUCT_PARAM pStoredItem STORED_ITEM_DATA 0
    WRITE_STRUCT_PARAM pStoredItem STORED_COUNT 0
    WRITE_STRUCT_PARAM pStoredItem STORED_FLAGS 0
    CLEO_RETURN 0 ()
}

{
    LVAR_INT pDragStoredItem iSizeOfEachStoredItem
    AllocateDragStoredItem:    
    iSizeOfEachStoredItem = STORED_TOTAL_PARAMS * 4
    ALLOCATE_MEMORY iSizeOfEachStoredItem (pDragStoredItem)
    CLEO_CALL InitEmptyStoredItem 0 (pDragStoredItem)
    CLEO_RETURN 0 (pDragStoredItem)
}

{
    LVAR_INT pFindItemName lItemsNames lItemsData //In
    LVAR_INT i iListSize pThisItemName pItemData
    LVAR_TEXT_LABEL16 tThisItemName

    FindItemDataByName:
    GET_LIST_SIZE lItemsNames (iListSize)
    i = 0
    WHILE i < iListSize
        GET_LIST_STRING_VALUE_BY_INDEX lItemsNames i (tThisItemName)
        //PRINT_FORMATTED_NOW "comparing '%s'" 2000 $tThisItemName
        //WAIT 2000
        IF IS_STRING_EQUAL $pFindItemName $tThisItemName 8 FALSE ""
            GET_LIST_VALUE_BY_INDEX lItemsData i (pItemData)
            RETURN_TRUE
            CLEO_RETURN 0 (pItemData)
        ENDIF
        ++i
    ENDWHILE
    RETURN_FALSE
    CLEO_RETURN 0 (0)
}

{
    LVAR_INT hChar pStoredItem lItemsNames hRenderObjectItem //In
    LVAR_INT pStoredItemData iModel iBone iItemID irX irY irZ pBuffer pBuffer2 i bDisableCmd iMode hObject
    LVAR_FLOAT x y z rX rY rZ rGroundX rGroundY rGroundZ groundHeight f
    LVAR_TEXT_LABEL tText

    ItemEdit:
    READ_STRUCT_PARAM pStoredItem STORED_ITEM_DATA pStoredItemData
    IF pStoredItemData > 0
        READ_STRUCT_PARAM pStoredItemData DATA_ITEM_ID iItemID
        IF iItemID > -1
            READ_STRUCT_PARAM pStoredItemData DATA_MODEL_ID iModel
            READ_STRUCT_PARAM pStoredItemData DATA_BONE iBone
            GET_LABEL_POINTER Buffer pBuffer
            GET_LABEL_POINTER Buffer2 pBuffer2
            WHILE NOT TEST_CHEAT ISITEMEDIT
                WAIT 0

                IF iMode = 0
                    READ_STRUCT_PARAM pStoredItemData DATA_OFFSET_X (x)
                    READ_STRUCT_PARAM pStoredItemData DATA_OFFSET_Y (y)
                    READ_STRUCT_PARAM pStoredItemData DATA_OFFSET_Z (z)
                    READ_STRUCT_PARAM pStoredItemData DATA_ROTATION_X (rX)
                    READ_STRUCT_PARAM pStoredItemData DATA_ROTATION_Y (rY)
                    READ_STRUCT_PARAM pStoredItemData DATA_ROTATION_Z (rZ)
                    IF hRenderObjectItem > 0x0
                        DELETE_RENDER_OBJECT hRenderObjectItem
                    ENDIF
                    IF NOT IS_KEY_PRESSED 107
                    AND NOT IS_KEY_PRESSED VK_OEM_PLUS
                    AND NOT IS_KEY_PRESSED 109
                    AND NOT IS_KEY_PRESSED VK_OEM_MINUS
                        bDisableCmd = FALSE
                    ENDIF
                    IF IS_KEY_PRESSED VK_KEY_B
                        IF IS_KEY_PRESSED 107
                        OR IS_KEY_PRESSED VK_OEM_PLUS
                            IF bDisableCmd = FALSE
                                iBone += 1
                                bDisableCmd = TRUE
                            ENDIF
                        ENDIF
                        IF IS_KEY_PRESSED 109
                        OR IS_KEY_PRESSED VK_OEM_MINUS
                            IF bDisableCmd = FALSE
                                iBone -= 1
                                bDisableCmd = TRUE
                            ENDIF
                        ENDIF
                    ENDIF
                    IF IS_KEY_PRESSED VK_KEY_R
                        GOSUB ItemEdit_Rotation
                    ELSE
                        GOSUB ItemEdit_PosX
                        GOSUB ItemEdit_PosY
                        GOSUB ItemEdit_PosZ
                    ENDIF
                    IF IS_KEY_JUST_PRESSED VK_RETURN
                        IF x < 0.01
                        AND x > -0.01
                            x = 0.0
                        ENDIF
                        IF y < 0.01
                        AND y > -0.01
                            y = 0.0
                        ENDIF
                        IF z < 0.01
                        AND z > -0.01
                            z = 0.0
                        ENDIF
                        irX =# rX
                        irY =# rY
                        irZ =# rZ
                        STRING_FORMAT pBuffer2 "%i %i %i" irX irY irZ
                        GET_LIST_STRING_VALUE_BY_INDEX lItemsNames iItemID tText
                        STRING_FORMAT pBuffer "modloader\Inventory System\CLEO\IS Items\%s.ini" $tText
                        WRITE_STRING_TO_INI_FILE $pBuffer2 $pBuffer "Data" "Rotation"
                        WRITE_FLOAT_TO_INI_FILE x $pBuffer "Data" "PosX"
                        WRITE_FLOAT_TO_INI_FILE y $pBuffer "Data" "PosY"
                        WRITE_FLOAT_TO_INI_FILE z $pBuffer "Data" "PosZ"
                        WRITE_INT_TO_INI_FILE iBone $pBuffer "Data" "BodyPart"
                        PRINT_FORMATTED_NOW "Saved to '%s'" 1000 $tText
                    ENDIF
                    DRAW_STRING "B + +/- to change body part" DRAW_EVENT_AFTER_HUD 100.0 300.0 0.4 0.8 TRUE FONT_SUBTITLES
                    DRAW_STRING "X/Y/Z + +/- to move" DRAW_EVENT_AFTER_HUD 100.0 320.0 0.4 0.8 TRUE FONT_SUBTITLES
                    DRAW_STRING "R + X/Y/Z + +/- to rotate" DRAW_EVENT_AFTER_HUD 100.0 340.0 0.4 0.8 TRUE FONT_SUBTITLES
                    DRAW_STRING "Enter to save" DRAW_EVENT_AFTER_HUD 100.0 360.0 0.4 0.8 TRUE FONT_SUBTITLES
                    DRAW_STRING "M to edit item on ground" DRAW_EVENT_AFTER_HUD 100.0 380.0 0.4 0.8 TRUE FONT_SUBTITLES
                    PRINT_FORMATTED "%i - %.3f %.3f %.3f ~n~ %.0f %.0f %.0f" 100 iBone x y z rX rY rZ
                    CREATE_RENDER_OBJECT_TO_CHAR_BONE hChar iModel iBone x y z rX rY rZ (hRenderObjectItem)
                    WRITE_STRUCT_PARAM pStoredItemData DATA_OFFSET_X x
                    WRITE_STRUCT_PARAM pStoredItemData DATA_OFFSET_Y y
                    WRITE_STRUCT_PARAM pStoredItemData DATA_OFFSET_Z z
                    WRITE_STRUCT_PARAM pStoredItemData DATA_ROTATION_X rX
                    WRITE_STRUCT_PARAM pStoredItemData DATA_ROTATION_Y rY
                    WRITE_STRUCT_PARAM pStoredItemData DATA_ROTATION_Z rZ
                    IF IS_KEY_JUST_PRESSED VK_KEY_M
                        iMode = 1
                        // simple drop position script, not totally safe
                        GET_OFFSET_FROM_CHAR_IN_WORLD_COORDS hChar 0.0 1.0 0.2 (x y z) // ground in front of CJ
                        GET_GROUND_Z_FOR_3D_COORD x y z (f)
                        IF f = 0.0
                            GET_OFFSET_FROM_CHAR_IN_WORLD_COORDS hChar 0.0 0.0 0.0 (x y z) // ground in CJ position
                            GET_GROUND_Z_FOR_3D_COORD x y z (f)
                            IF f = 0.0
                                f = z // can't find ground, use CJ position
                            ENDIF
                        ENDIF
                        z = f + groundHeight
                        CREATE_OBJECT_NO_SAVE iModel x y z FALSE FALSE (hObject)
                        CONTINUE
                    ENDIF
                ENDIF

                IF iMode = 1
                    IF IS_KEY_PRESSED VK_KEY_R
                        GOSUB ItemEdit_Rotation
                    ELSE
                        GOSUB ItemEdit_GroundHeight
                    ENDIF
                    IF IS_KEY_JUST_PRESSED VK_RETURN
                        IF groundHeight < 0.01
                        AND groundHeight > -0.01
                            groundHeight = 0.0
                        ENDIF
                        groundHeight *= 100.0
                        i =# groundHeight
                        irX =# rX
                        irY =# rY
                        irZ =# rZ
                        STRING_FORMAT pBuffer2 "%i %i %i %i" i irX irY irZ
                        WRITE_CLIPBOARD_DATA_FROM pBuffer2 128
                        //PRINT_FORMATTED_NOW "Copied to clipboard: '%s'" 2000 $pBuffer2
                        groundHeight /= 100.0
                    ENDIF
                    DRAW_STRING "Z + +/- to change height" DRAW_EVENT_AFTER_HUD 100.0 300.0 0.4 0.8 TRUE FONT_SUBTITLES
                    DRAW_STRING "R + X/Y/Z + +/- to rotate" DRAW_EVENT_AFTER_HUD 100.0 320.0 0.4 0.8 TRUE FONT_SUBTITLES
                    DRAW_STRING "Enter to copy to clipboard" DRAW_EVENT_AFTER_HUD 100.0 340.0 0.4 0.8 TRUE FONT_SUBTITLES
                    DRAW_STRING "Paste into .ini and retart to update values" DRAW_EVENT_AFTER_HUD 100.0 360.0 0.4 0.8 TRUE FONT_SUBTITLES
                    DRAW_STRING "M to edit item on body" DRAW_EVENT_AFTER_HUD 100.0 380.0 0.4 0.8 TRUE FONT_SUBTITLES
                    PRINT_FORMATTED "%.3f ~n~ %.0f %.0f %.0f" 100 groundHeight rX rY rZ
                    z = f + groundHeight
                    SET_OBJECT_COORDINATES hObject x y z
                    SET_OBJECT_ROTATION hObject rX rY rZ
                    IF IS_KEY_JUST_PRESSED VK_KEY_M
                        iMode = 0
                        DELETE_OBJECT hObject
                        hObject = -1
                    ENDIF
                ENDIF

            ENDWHILE
        ENDIF
    ENDIF
    IF hObject >= -1
        DELETE_OBJECT hObject //recreate because SET_OBJECT_COORDINATES doesn't include offset
    ENDIF
    CLEO_RETURN 0 (hRenderObjectItem)

    ItemEdit_Rotation:
    IF IS_KEY_PRESSED VK_KEY_X
        IF IS_KEY_PRESSED 107
        OR IS_KEY_PRESSED VK_OEM_PLUS
            rX +=@ 1.0
        ENDIF
        IF IS_KEY_PRESSED 109
        OR IS_KEY_PRESSED VK_OEM_MINUS
            rX -=@ 1.0
        ENDIF
    ENDIF
    IF IS_KEY_PRESSED VK_KEY_Y
        IF IS_KEY_PRESSED 107
        OR IS_KEY_PRESSED VK_OEM_PLUS
            rY +=@ 1.0
        ENDIF
        IF IS_KEY_PRESSED 109
        OR IS_KEY_PRESSED VK_OEM_MINUS
            rY -=@ 1.0
        ENDIF
    ENDIF
    IF IS_KEY_PRESSED VK_KEY_Z
        IF IS_KEY_PRESSED 107
        OR IS_KEY_PRESSED VK_OEM_PLUS
            rZ +=@ 1.0
        ENDIF
        IF IS_KEY_PRESSED 109
        OR IS_KEY_PRESSED VK_OEM_MINUS
            rZ -=@ 1.0
        ENDIF
    ENDIF
    WHILE rX < 0.0
        rX += 360.0
    ENDWHILE
    WHILE rY < 0.0
        rY += 360.0
    ENDWHILE
    WHILE rZ < 0.0
        rZ += 360.0
    ENDWHILE
    WHILE rX > 360.0
        rX -= 360.0
    ENDWHILE
    WHILE rY > 360.0
        rY -= 360.0
    ENDWHILE
    WHILE rZ > 360.0
        rZ -= 360.0
    ENDWHILE
    RETURN
    
    ItemEdit_PosX:
    IF IS_KEY_PRESSED VK_KEY_X
        IF IS_KEY_PRESSED 107
        OR IS_KEY_PRESSED VK_OEM_PLUS
            x +=@ 0.001
        ENDIF
        IF IS_KEY_PRESSED 109
        OR IS_KEY_PRESSED VK_OEM_MINUS
            x -=@ 0.001
        ENDIF
    ENDIF
    RETURN
    ItemEdit_PosY:
    IF IS_KEY_PRESSED VK_KEY_Y
        IF IS_KEY_PRESSED 107
        OR IS_KEY_PRESSED VK_OEM_PLUS
            y +=@ 0.001
        ENDIF
        IF IS_KEY_PRESSED 109
        OR IS_KEY_PRESSED VK_OEM_MINUS
            y -=@ 0.001
        ENDIF
    ENDIF
    RETURN
    ItemEdit_PosZ:
    IF IS_KEY_PRESSED VK_KEY_Z
        IF IS_KEY_PRESSED 107
        OR IS_KEY_PRESSED VK_OEM_PLUS
            z +=@ 0.001
        ENDIF
        IF IS_KEY_PRESSED 109
        OR IS_KEY_PRESSED VK_OEM_MINUS
            z -=@ 0.001
        ENDIF
    ENDIF
    RETURN
    ItemEdit_GroundHeight:
    IF IS_KEY_PRESSED VK_KEY_Z
        IF IS_KEY_PRESSED 107
        OR IS_KEY_PRESSED VK_OEM_PLUS
            groundHeight +=@ 0.001
        ENDIF
        IF IS_KEY_PRESSED 109
        OR IS_KEY_PRESSED VK_OEM_MINUS
            groundHeight -=@ 0.001
        ENDIF
    ENDIF
    RETURN
}

{
    LVAR_INT lItemsNames lItemsData lItemsScripts //In
    LVAR_INT iFind pBuffer pBuffer2 pShortBuffer pDataStruct iItemsCount p i j pShortName pZ pY pX
    LVAR_FLOAT x y z

    ReadStoreItemsData:
    GET_LABEL_POINTER ShortBuffer pShortBuffer
    IF FIND_FIRST_FILE "modloader\Inventory System\CLEO\IS Items\*.ini" iFind pShortBuffer
        WHILE TRUE
            GOSUB ReadStoreItemsData_ReadOne
            IF NOT FIND_NEXT_FILE iFind pShortBuffer
                BREAK
            ENDIF
        ENDWHILE
    ENDIF
    //PRINT_FORMATTED_NOW "total items %i" 1000 iItemsCount
    CLEO_RETURN 0 ()

    ReadStoreItemsData_ReadOne:
    // remove '.ini' from pBuffer
    GET_STRING_LENGTH $pShortBuffer (i)
    i -= 4 // remove '.ini'
    IF i > 7 // item name is too long
        PRINT_FORMATTED_NOW "ERROR item name too long '%s'" 5000 $pShortBuffer
        WAIT 3000
        RETURN
    ENDIF
    i = pShortBuffer + i
    WRITE_MEMORY i 1 0x0 FALSE

    //PRINT_FORMATTED_NOW "adding list name %s index %i" 5000 $pShortBuffer iItemsCount
    //WAIT 500

    // store item short name
    LIST_ADD_STRING lItemsNames $pShortBuffer

    // store allocate data struct
    i = DATA_TOTAL_PARAMS * 4
    ALLOCATE_MEMORY i (pDataStruct)
    WRITE_MEMORY pDataStruct i 0x00 FALSE //clear bytes
    LIST_ADD lItemsData (pDataStruct)

    // -- read ini
    GET_LABEL_POINTER Buffer pBuffer
    GET_LABEL_POINTER Buffer2 pBuffer2
    STRING_FORMAT pBuffer "modloader\Inventory System\CLEO\IS Items\%s.ini" $pShortBuffer

    //PRINT_FORMATTED_NOW "reading '%s'" 4000 $pBuffer 
    //WAIT 3000

    // Set item ID
    WRITE_STRUCT_PARAM pDataStruct DATA_ITEM_ID iItemsCount

    // read model name
    i = -1
    IF READ_STRING_FROM_INI_FILE $pBuffer "Data" "ModelName" (pBuffer2)
        IF NOT GET_MODEL_BY_NAME $pBuffer2 (i)
            PRINT_FORMATTED_NOW "Can't find model '%s' from '%s'" 5000 $pBuffer2 $pBuffer
            WAIT 5000
        ENDIF
    ENDIF

    WRITE_STRUCT_PARAM pDataStruct DATA_MODEL_ID i

    // read bone (body part)
    IF NOT READ_INT_FROM_INI_FILE $pBuffer "Data" "BodyPart" (i)
        i = 0
    ENDIF

    WRITE_STRUCT_PARAM pDataStruct DATA_BONE i

    // read stack
    IF NOT READ_INT_FROM_INI_FILE $pBuffer "Data" "Stack" (i)
        i = 0
    ENDIF
    
    WRITE_STRUCT_PARAM pDataStruct DATA_STACK i

    // read offset
    IF NOT READ_FLOAT_FROM_INI_FILE $pBuffer "Data" "PosX" (x)
        x = 0.0
    ENDIF
    IF NOT READ_FLOAT_FROM_INI_FILE $pBuffer "Data" "PosY" (y)
        y = 0.0
    ENDIF
    IF NOT READ_FLOAT_FROM_INI_FILE $pBuffer "Data" "PosZ" (z)
        z = 0.0
    ENDIF
    WRITE_STRUCT_PARAM pDataStruct DATA_OFFSET_X x
    WRITE_STRUCT_PARAM pDataStruct DATA_OFFSET_Y y
    WRITE_STRUCT_PARAM pDataStruct DATA_OFFSET_Z z

    // read rotation
    GET_VAR_POINTER x (pX)
    GET_VAR_POINTER y (pY)
    GET_VAR_POINTER z (pZ)
    x = 0.0
    y = 0.0
    z = 0.0
    IF READ_STRING_FROM_INI_FILE $pBuffer "Data" "Rotation" (pBuffer2)
        GET_STRING_LENGTH $pBuffer2 i
        IF i > 0
            CALL_FUNCTION_RETURN 0x8220AD 5 5 (pZ pY pX, "%f%f%f" pBuffer2)(i)
        ENDIF
    ENDIF
    WRITE_STRUCT_PARAM pDataStruct DATA_ROTATION_X x
    WRITE_STRUCT_PARAM pDataStruct DATA_ROTATION_Y y
    WRITE_STRUCT_PARAM pDataStruct DATA_ROTATION_Z z


    // read use scripts

    // Main
    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Main" "Script" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2
    
    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Main" "Values" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2

    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Main" "Icon" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2


    // Pos
    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Pos" "Script" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2
    
    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Pos" "Values" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2

    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Pos" "Icon" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2
    

    // Additional A
    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Additional A" "Script" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2
    
    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Additional A" "Values" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2

    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Additional A" "Icon" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2
    

    // Additional B
    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Additional B" "Script" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2
    
    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Additional B" "Values" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2

    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Use Additional B" "Icon" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2
    

    // Events
    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Events" "Script" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2
    
    IF NOT READ_STRING_FROM_INI_FILE $pBuffer "Events" "Values" (pBuffer2)
        WRITE_MEMORY pBuffer2 1 0x0 FALSE
    ENDIF
    LIST_ADD_STRING lItemsScripts $pBuffer2


    ++iItemsCount
    RETURN
}

ShortBuffer:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //32
ENDDUMP

Buffer:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //64
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //128
ENDDUMP

Buffer2:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //64
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //128
ENDDUMP

ReallyTempBuffer:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //64
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //128
ENDDUMP

InventorySpaces:
DUMP
00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00
00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00
00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00  00 00 00 00
ENDDUMP 

Original_5408B3_Bytes:
DUMP
66 83 BF 0E 01 00 00 00
ENDDUMP
