// by Junior_Djjr - MixMods.com.br
// You need: https://forum.mixmods.com.br/f141-gta3script-cleo/t5206-como-criar-scripts-com-cleo
SCRIPT_START
{
    LVAR_INT scplayer i j k l pItemName pMenuName lMenuItems lMenuPrices hMenu iPrice bIsFood iTotalItems
    LVAR_FLOAT x y z fClosestDistance screenX screenY sizeX sizeY charX charY charZ f shopX shopY shopZ shopAngle
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

    GET_PLAYER_CHAR 0 scplayer

    //SET_CHAR_COORDINATES scplayer 1833.294312 -1843.247559 13.3 //tests

    GET_LABEL_POINTER MenuName (pMenuName)
    GET_LABEL_POINTER ItemNameBuffer (pItemName)

    CREATE_LIST DATATYPE_STRING lMenuItems
    CREATE_LIST DATATYPE_INT lMenuPrices


    WHILE TRUE
        WAIT 0

        GET_AREA_VISIBLE (i)
        IF i > 0

            shopX = 378.3681  
            shopY = -68.34 
            shopZ = 1001.5151
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY shopZ 30.0 30.0 30.0 FALSE
                shopAngle = 0.0
                bIsFood = TRUE
                COPY_STRING "Burger Shot" pMenuName
                GOSUB RunShop
            ENDIF

            shopX = 377.4678 
            shopY = -119.6362 
            shopZ = 1001.4995
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY shopZ 30.0 30.0 30.0 FALSE
                shopAngle = 0.0
                bIsFood = TRUE
                COPY_STRING "Pizza Stack" pMenuName
                GOSUB RunShop
            ENDIF

            shopX = 371.6555 
            shopY = -7.0581 
            shopZ = 1001.8589
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY shopZ 30.0 30.0 30.0 FALSE
                shopAngle = 0.0
                bIsFood = TRUE
                COPY_STRING "Cluckin Bell" pMenuName
                GOSUB RunShop
            ENDIF

            shopX = 500.8591 
            shopY = -75.7304 
            shopZ = 998.7578
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY shopZ 30.0 30.0 30.0 FALSE
                shopAngle = 180.0
                bIsFood = TRUE
                COPY_STRING "Bar" pMenuName
                GOSUB RunShop
            ENDIF

            shopX = 499.7869 
            shopY = -23.9551 
            shopZ = 1000.6797
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY shopZ 30.0 30.0 30.0 FALSE
                shopAngle = 270.0
                bIsFood = TRUE
                COPY_STRING "Bar" pMenuName
                GOSUB RunShop
            ENDIF

            shopX = 380.7933 
            shopY = -191.0857 
            shopZ = 1000.6328
            IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY shopZ 30.0 30.0 30.0 FALSE
                shopAngle = 0.0
                bIsFood = TRUE
                COPY_STRING "Donuts" pMenuName
                GOSUB RunShop
            ENDIF

        ENDIF

    ENDWHILE


    RunShop:
    CLEO_CALL ReadIni 0 (pMenuName lMenuItems lMenuPrices)
    GOSUB ProcessShop
    RETURN


    ProcessShop:
    GET_LIST_SIZE lMenuItems iTotalItems

    WHILE TRUE
        WAIT 0

        GET_CHAR_AREA_VISIBLE scplayer i
        
        IF LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY shopZ 50.0 50.0 40.0 FALSE
        AND i > 0

            IF iTotalItems > 0

                IF LOCATE_STOPPED_CHAR_ON_FOOT_3D scplayer shopX shopY shopZ 1.3 1.3 1.3 TRUE

                    SET_PLAYER_CONTROL 0 OFF
                    TASK_ACHIEVE_HEADING scplayer shopAngle
                
                    CLEO_CALL CreateMenu 0 (lMenuItems lMenuPrices)(hMenu)

                    WHILE NOT IS_BUTTON_JUST_PRESSED PAD1 TRIANGLE
                        WAIT 0
                        IF IS_SELECT_MENU_JUST_PRESSED
                            GET_MENU_ITEM_SELECTED hMenu i
                            
                            GET_LIST_VALUE_BY_INDEX lMenuPrices i (iPrice)
                            IF IS_SCORE_GREATER 0 iPrice
                                GET_LIST_STRING_VALUE_BY_INDEX lMenuItems i tText

                                GET_LABEL_POINTER BufferItemGive (j)
                                STRING_FORMAT j "%s" $tText
                                i = EXTERNAL_ACTION_GIVE_ITEM //char; item name pointer or ID
                                IF STREAM_CUSTOM_SCRIPT "Inventory System (Junior_Djjr).cs" i scplayer j
                                    //REPORT_MISSION_AUDIO_EVENT_AT_POSITION 0.0 0.0 0.0 SOUND_SHOP_BUY
                                    CHANGE_PLAYER_MONEY 0 CHANGE_MONEY_REMOVE iPrice //NOTE: this will run even if the item isn't installed
                                    f =# iPrice
                                    IF bIsFood = TRUE
                                        // If someone adds non-food to food store, it will still be consired food budget, but nobody cares
                                        INCREMENT_FLOAT_STAT 20 f //STAT_FOOD_BUDGET
                                    ENDIF
                                    INCREMENT_FLOAT_STAT 62 f //STAT_TOTAL_SHOPPING_BUDGET
                                ELSE
                                    PRINT_STRING_NOW "~r~Error: Can't find 'Inventory System (Junior_Djjr).cs'" 5000
                                ENDIF
                            ELSE
                                REPORT_MISSION_AUDIO_EVENT_AT_POSITION 0.0 0.0 0.0 SOUND_SHOP_BUY_DENIED
                                PRINT_NOW SHOPNO 3000 1 // ~s~You don't have enough money to buy this item.
                            ENDIF

                        ENDIF
                    ENDWHILE

                    DELETE_MENU hMenu
                    SET_PLAYER_CONTROL 0 ON

                    WHILE LOCATE_CHAR_ANY_MEANS_3D scplayer shopX shopY shopZ 1.3 1.3 1.3 FALSE
                        WAIT 0
                    ENDWHILE

                ENDIF
            ENDIF
        
        ELSE
            BREAK
        ENDIF
    ENDWHILE
    RETURN


}
SCRIPT_END

{
    LVAR_INT lMenuItems lMenuPrices //In
    LVAR_INT hMenu iPrice i j
    LVAR_TEXT_LABEL tItem

    CreateMenu:

    //08D4=9,create_menu %1g% position %2d% %3d% width %4d% columns %5h% interactive %6h% background %7h% alignment %8h% store_to %9d%
    CREATE_MENU ISSHP02 (20.0 150.0) (125.0) 2 (ON ON 1) (hMenu)

    //08DB=15,set_menu_column %1d% col %2h% title_to %3g% items_to %4g% %5g% %6g% %7g% %8g% %9g%   %10g% %11g% %12g% %13g% %14g% %15g%
    SET_MENU_COLUMN hMenu 0 ISSHP03 (DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY) // Item
    SET_MENU_COLUMN hMenu 1 ISSHP04 (DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY DUMMY) // Price
    
    SET_MENU_COLUMN_ORIENTATION hMenu 0 1
    SET_MENU_COLUMN_WIDTH hMenu 1 50
    SET_ACTIVE_MENU_ITEM hMenu 0
    
    GET_LIST_SIZE lMenuItems j
    IF j > 12
        j = 12 // max 12 itens
    ENDIF

    i = 0
    WHILE i < j
        GET_LIST_STRING_VALUE_BY_INDEX lMenuItems i tItem
        GET_LIST_VALUE_BY_INDEX lMenuPrices i iPrice

        // 08EE: set_panel $IMPORT_CAR_PANEL column 1 row 4@ text_1number GXT 'DOLLAR' number $IMPORT_CAR_PRICE  // $~1~
        SET_MENU_ITEM_WITH_NUMBER hMenu 0 i $tItem 0 // number is ignored here
        SET_MENU_ITEM_WITH_NUMBER hMenu 1 i ISSHP05 iPrice //$~1~

        ++i
    ENDWHILE

    CLEO_RETURN 0 (hMenu)
}

{
    LVAR_INT pMenuName lMenuItems lMenuPrices //In
    LVAR_INT i j iItem pItemName iPrice iObjectModel pBuffer pBuffer2 iFind px py pz pRootScript lItemsData iItemID iTotalItemsInstalled pItemNameBuffer
    LVAR_FLOAT x y z
    LVAR_TEXT_LABEL tItemName

    ReadIni:

    RESET_LIST lMenuItems
    RESET_LIST lMenuPrices

    GET_LABEL_POINTER Buffer (pBuffer)
    GET_LABEL_POINTER Buffer2 (pBuffer2)
    GET_LABEL_POINTER ItemNameBuffer (pItemNameBuffer)

    i = 1
    WHILE i <= 7
        STRING_FORMAT pBuffer "Item%i" i

        IF READ_STRING_FROM_INI_FILE "CLEO\IS Shop Menu.ini" $pMenuName $pBuffer (pItemNameBuffer)
            LIST_ADD_STRING lMenuItems $pItemNameBuffer

            STRING_FORMAT pBuffer2 "Item%iPrice" i
            IF NOT READ_INT_FROM_INI_FILE "CLEO\IS Shop Menu.ini" $pMenuName $pBuffer2 (j)
                j = 0
            ENDIF
            LIST_ADD lMenuPrices j
        ELSE
            BREAK
        ENDIF
        ++i
    ENDWHILE

    //PRINT_FORMATTED_NOW "total %d" 2000 i

    CLEO_RETURN 0 ()
    
}

MenuName:
DUMP
00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 //64
ENDDUMP

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
