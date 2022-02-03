// by Junior_Djjr - MixMods.com.br
// You need: https://forum.mixmods.com.br/f141-gta3script-cleo/t5206-como-criar-scripts-com-cleo
SCRIPT_START
{
    LVAR_INT scplayer i j k l lBuyList pItemName lShopListNames lShopListPrices lShopListModels lShopListX lShopListY lShopListZ iTotalShopItems iClosestShopItemID pClosestEntity
    LVAR_FLOAT x y z fClosestDistance screenX screenY sizeX sizeY charX charY charZ f shopX shopY
    LVAR_TEXT_LABEL tText

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

    CONST_FLOAT MIN_DISTANCE_TO_SHOW 7.0
    CONST_FLOAT MIN_DISTANCE_TO_PROCESS 12.0
    CONST_FLOAT MIN_DISTANCE_TO_PICK 1.0
    CONST_FLOAT SHOP_ITEM_LETTER_SIZE_MULT 0.3

    WAIT 0
    WAIT 0
    WAIT 0 // to give time for items to be read on script root

    CREATE_LIST DATATYPE_STRING lShopListNames
    CREATE_LIST DATATYPE_INT lShopListPrices
    CREATE_LIST DATATYPE_INT lShopListModels
    CREATE_LIST DATATYPE_FLOAT lShopListX
    CREATE_LIST DATATYPE_FLOAT lShopListY
    CREATE_LIST DATATYPE_FLOAT lShopListZ
    CREATE_LIST DATATYPE_STRING lBuyList

    GET_PLAYER_CHAR 0 scplayer

    //SET_CHAR_COORDINATES scplayer 1833.294312 -1843.247559 13.3 //tests

    GET_LABEL_POINTER ItemNameBuffer (pItemName)

    CLEO_CALL ReadIni 0 (lShopListNames lShopListPrices lShopListModels lShopListX lShopListY lShopListZ)


    WHILE TRUE
        WAIT 0

        GET_AREA_VISIBLE (i)
        IF i > 0

            shopX = -26.2593 
            shopY = -81.3956 
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY 1000.0 40.0 40.0 30.0 FALSE
                GOSUB RunShop
            ENDIF

            shopX = -27.669363 
            shopY = -52.307713
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY 1000.0 30.0 30.0 30.0 FALSE
                GOSUB RunShop
            ENDIF

            shopX = -31.268250 
            shopY = -17.123274 
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY 1000.0 40.0 40.0 30.0 FALSE
                GOSUB RunShop
            ENDIF

            shopX = -27.835342 
            shopY = -51.975571 
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY 1000.0 30.0 30.0 30.0 FALSE
                GOSUB RunShop
            ENDIF

            shopX = -20.531492 
            shopY = -175.396942
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY 1000.0 50.0 50.0 30.0 FALSE
                GOSUB RunShop
            ENDIF
        ENDIF

    ENDWHILE


    RunShop:
    SET_SCRIPT_EVENT_BUILDING_PROCESS ON OnBuildingProcess j
    GOSUB ProcessShop
    SET_SCRIPT_EVENT_BUILDING_PROCESS OFF OnBuildingProcess j
    RETURN


    ProcessShop:
    WHILE TRUE
        fClosestDistance = 9999999.9
        iClosestShopItemID = -1
        GET_CHAR_COORDINATES scplayer (charX charY charZ)

        WAIT 0 // OnBuildingProcess will run on every entity now

        GET_AREA_VISIBLE (i)
        
        IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY 1000.0 50.0 50.0 20.0 FALSE
        AND i > 0
            IF NOT iClosestShopItemID = -1
                IF fClosestDistance < MIN_DISTANCE_TO_PICK
                
                    READ_STRUCT_OFFSET pClosestEntity 0x14 4 (timerb) //pMatrix
                    IF timerb > 0x0

                        GET_LIST_VALUE_BY_INDEX lShopListX iClosestShopItemID (x)
                        GET_LIST_VALUE_BY_INDEX lShopListY iClosestShopItemID (y)
                        GET_LIST_VALUE_BY_INDEX lShopListZ iClosestShopItemID (z)
                        GET_OFFSET_FROM_MATRIX_IN_WORLD_COORDS timerb x y z (x y z)
                        
                        WRITE_MEMORY 0xB7CD68 4 pClosestEntity FALSE
                        IF IS_LINE_OF_SIGHT_CLEAR x y z charX charY charZ 1 0 0 0 0
                            f = z + 0.2
                            CONVERT_3D_TO_SCREEN_2D x y f TRUE FALSE (screenX screenY sizeX sizeY)
                            sizeX *= SHOP_ITEM_LETTER_SIZE_MULT
                            sizeY *= SHOP_ITEM_LETTER_SIZE_MULT
                            sizeX *= 0.8
                            sizeY *= 1.5
                            GET_FIXED_XY_ASPECT_RATIO sizeX sizeY (sizeX sizeY)

                            USE_TEXT_COMMANDS 1
                            SET_TEXT_FONT FONT_SUBTITLES
                            SET_TEXT_EDGE 1 0 0 0 255
                            SET_TEXT_CENTRE ON
                            SET_TEXT_SCALE sizeX sizeY
                            //ISSHP01 ~s~Press ~y~~k~~CONVERSATION_YES~~s~+ / ~y~~k~~CONVERSATION_NO~~s~-
                            //ISSHP01 ~s~Press ~y~~k~~VEHICLE_ENTER_EXIT~
                            DISPLAY_TEXT screenX screenY ISSHP01
                            USE_TEXT_COMMANDS 0

                            IF IS_BUTTON_JUST_PRESSED PAD1 TRIANGLE
                                GET_LIST_VALUE_BY_INDEX lShopListPrices iClosestShopItemID (timera)
                                IF IS_SCORE_GREATER 0 timera
                                    GET_LIST_STRING_VALUE_BY_INDEX lShopListNames iClosestShopItemID tText

                                    GET_LABEL_POINTER BufferItemGive (j)
                                    STRING_FORMAT j "%s" $tText
                                    i = EXTERNAL_ACTION_GIVE_ITEM //char; item name pointer or ID
                                    IF STREAM_CUSTOM_SCRIPT "Inventory System (Junior_Djjr).cs" i scplayer j
                                        REPORT_MISSION_AUDIO_EVENT_AT_POSITION 0.0 0.0 0.0 SOUND_SHOP_BUY
                                        CHANGE_PLAYER_MONEY 0 CHANGE_MONEY_REMOVE timera //NOTE: this will run even if the item isn't installed
                                        f =# timera
                                        INCREMENT_FLOAT_STAT 20 f //STAT_FOOD_BUDGET
                                        INCREMENT_FLOAT_STAT 62 f //STAT_TOTAL_SHOPPING_BUDGET
                                    ELSE
                                        PRINT_STRING_NOW "~r~Error: Can't find 'Inventory System (Junior_Djjr).cs'" 5000
                                    ENDIF
                                ELSE
                                    REPORT_MISSION_AUDIO_EVENT_AT_POSITION 0.0 0.0 0.0 SOUND_SHOP_BUY_DENIED
                                    PRINT_NOW SHOPNO 3000 1 // ~s~You don't have enough money to buy this item.
                                ENDIF

                            ENDIF
                        ENDIF
                        WRITE_MEMORY 0xB7CD68 4 0 FALSE
                    ENDIF
                ENDIF
            ENDIF
        ELSE
            BREAK
        ENDIF
    ENDWHILE
    RETURN

    OnBuildingProcess:
    READ_STRUCT_OFFSET j 0x22 2 (k) // entity model ID

    // limit ids, just to make it faster
    IF k > 1900
    AND k < 3000

        READ_STRUCT_OFFSET j 0x14 4 (timera) //pMatrix
        IF timera > 0x0
        
            GET_OFFSET_FROM_MATRIX_IN_WORLD_COORDS timera 0.0 0.0 0.0 (x y z)

            GET_DISTANCE_BETWEEN_COORDS_2D x y charX charY (f)
            IF f < MIN_DISTANCE_TO_PROCESS
                
                i = 0
                GET_LIST_SIZE lShopListNames (iTotalShopItems)
                WHILE i < iTotalShopItems
                    GET_LIST_VALUE_BY_INDEX lShopListModels i (timerb)

                    IF timerb = k // entity model is correct

                        GET_LIST_VALUE_BY_INDEX lShopListX i (x)
                        GET_LIST_VALUE_BY_INDEX lShopListY i (y)
                        GET_LIST_VALUE_BY_INDEX lShopListZ i (z)
                        READ_STRUCT_OFFSET j 0x14 4 (timera) //pMatrix
                        GET_OFFSET_FROM_MATRIX_IN_WORLD_COORDS timera x y z (x y z)

                        GET_DISTANCE_BETWEEN_COORDS_2D x y charX charY (f)
                        IF f < MIN_DISTANCE_TO_SHOW
                            WRITE_MEMORY 0xB7CD68 4 j FALSE
                            IF IS_LINE_OF_SIGHT_CLEAR x y z charX charY charZ 1 0 0 0 0

                                IF f < fClosestDistance
                                    fClosestDistance = f
                                    iClosestShopItemID = i
                                    pClosestEntity = j
                                ENDIF

                                CONVERT_3D_TO_SCREEN_2D x y z TRUE FALSE (screenX screenY sizeX sizeY)
                                sizeX *= SHOP_ITEM_LETTER_SIZE_MULT
                                sizeY *= SHOP_ITEM_LETTER_SIZE_MULT
                                GET_LIST_STRING_VALUE_BY_INDEX lShopListNames i tText
                                
                                GET_LABEL_POINTER Buffer (timera)
                                GET_TEXT_LABEL_STRING $tText (timera)
                                DRAW_STRING_EXT $timera DRAW_EVENT_BEFORE_DRAWING screenX screenY sizeX sizeY TRUE FONT_SUBTITLES TRUE ALIGN_CENTER 640.0 FALSE (255 255 255 255) 1 0 (0 0 0 255) FALSE (0 0 0 0)

                                f = z + 0.1
                                CONVERT_3D_TO_SCREEN_2D x y f TRUE FALSE (screenX screenY sizeX sizeY)
                                sizeX *= SHOP_ITEM_LETTER_SIZE_MULT
                                sizeY *= SHOP_ITEM_LETTER_SIZE_MULT
                                GET_LIST_VALUE_BY_INDEX lShopListPrices i (timerb)
                                GET_LABEL_POINTER Buffer2 (timera)
                                STRING_FORMAT timera "$%i" timerb
                                DRAW_STRING_EXT $timera DRAW_EVENT_BEFORE_DRAWING screenX screenY sizeX sizeY TRUE FONT_SUBTITLES TRUE ALIGN_CENTER 640.0 FALSE (100 255 0 255) 1 0 (0 0 0 255) FALSE (0 0 0 0)
                            ENDIF
                            WRITE_MEMORY 0xB7CD68 4 0 FALSE
                        ENDIF
                        
                    ENDIF
                    ++i
                ENDWHILE
            ENDIF
        ENDIF

    ENDIF

    RETURN_SCRIPT_EVENT

}
SCRIPT_END

{
    LVAR_INT pItemName iPrice iModel //In
    LVAR_FLOAT x y z //In
    LVAR_INT lShopListNames lShopListPrices lShopListModels lShopListX lShopListY lShopListZ //In

    AddItemToShopList:
    LIST_ADD_STRING lShopListNames $pItemName
    LIST_ADD lShopListPrices iPrice
    LIST_ADD lShopListModels iModel
    LIST_ADD lShopListX x
    LIST_ADD lShopListY y
    LIST_ADD lShopListZ z
    CLEO_RETURN 0 ()
}

{
    LVAR_INT lShopListNames lShopListPrices lShopListModels lShopListX lShopListY lShopListZ //In
    LVAR_INT i j iItem pItemName iPrice iObjectModel pBuffer pBuffer2 iFind px py pz pRootScript lItemsData iItemID iTotalItemsInstalled pItemNameBuffer
    LVAR_FLOAT x y z
    LVAR_TEXT_LABEL tItemName

    ReadIni:
    // Iterate all IS Items
    GET_SCRIPT_STRUCT_NAMED InvSyst (pRootScript)
    IF pRootScript > 0x0
        GET_VAR_POINTER x px
        GET_VAR_POINTER y py
        GET_VAR_POINTER z pz
        GET_LABEL_POINTER Buffer pBuffer
        GET_LABEL_POINTER Buffer2 pBuffer2
        GET_LABEL_POINTER ItemNameBuffer pItemNameBuffer
        GET_THREAD_VAR pRootScript VAR_ITEMS_NAMES lItemsData
        GET_LIST_SIZE lItemsData iTotalItemsInstalled
        iItemID = 0
        WHILE iItemID < iTotalItemsInstalled
            GET_LIST_STRING_VALUE_BY_INDEX lItemsData iItemID tItemName 
            GOSUB ReadIni_ReadOne
            ++iItemID
        ENDWHILE
    ELSE
        //PRINT_STRING_NOW "~r~No Inventory System script installed." 5000
    ENDIF
    CLEO_RETURN 0 ()

    ReadIni_ReadOne:
    IF NOT READ_INT_FROM_INI_FILE "CLEO\IS Shop 247.ini" $tItemName "Price" (iPrice)
        iPrice = 0
    ENDIF

    i = 1
    WHILE TRUE
        STRING_FORMAT pBuffer2 "Object%i" i

        IF NOT READ_INT_FROM_INI_FILE "CLEO\IS Shop 247.ini" $tItemName $pBuffer2 (iObjectModel)
            BREAK
        ENDIF
        STRING_FORMAT pBuffer2 "Offset%i" i
        IF READ_STRING_FROM_INI_FILE "CLEO\IS Shop 247.ini" $tItemName $pBuffer2 (pBuffer)
            //sscanf because SCAN_STRING doesn't work on current gta3script version
            CALL_FUNCTION_RETURN 0x8220AD 5 5 (pz py px, "%f%f%f" pBuffer)(j)
            IF NOT j = 3
                PRINT_FORMATTED_NOW "~r~IS Shop: Offset is badly configured on '%s'" 5000 $tItemName
                BREAK
            ENDIF
        ELSE
            PRINT_FORMATTED_NOW "~r~IS Shop: Object is set but no offset on '%s'" 5000 $tItemName
            BREAK
        ENDIF
        COPY_STRING $tItemName pItemNameBuffer //because current gta3script version doesn't support sending text labels to CLEO_CALL
        CLEO_CALL AddItemToShopList 0 (pItemNameBuffer iPrice iObjectModel x y z lShopListNames lShopListPrices lShopListModels lShopListX lShopListY lShopListZ)()
        ++i
    ENDWHILE
    RETURN
    
}

ItemNameBuffer:
DUMP
00 00 00 00 00 00 00 00 //8
ENDDUMP

BufferItemGive: /// this must keep for at least 1 frame
DUMP
00 00 00 00 00 00 00 00 //8
ENDDUMP

Buffer:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //64
ENDDUMP

Buffer2:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //64
ENDDUMP
