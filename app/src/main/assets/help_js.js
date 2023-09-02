/*
;########################################;
;                                        ;
; John Charles                           ; 
; Genesys East, LLC		                 ;
; Help_js.js                             ;
; August 27th, 2019                      ;
;                                        ;
;########################################;
*/

function getFromAndroid( colorType )
{
    var myVar;

    if ( typeof Android != 'undefined' )        
		myVar = Android.getFromAndroid( colorType );
    else
	{
		myVar = '#000000';
		/*switch ( colorType )
		{
			case 2 : myVar = '#008577'; break;
			case 1 : myVar = '#00574B'; break;
			default: myVar = '#163E2A'; break;
		}*/
	}
        

    // change the BG colors for Body or for div BG
    if ( colorType == 0 )
    {	// Change for Body BG
        document.body.style.background = myVar;
    }
    else 
	if ( colorType == 1 )
    {	// Change for div BG
        var divs = document.getElementsByTagName("div");

        for(var i = 0; i < divs.length; i++)
        {	//do something to each div
            divs[i].style.backgroundColor = myVar;
        }
    }	
	else 
	if ( colorType == 3 )
    {	// Change for buttons
		// Get the button
		var mybutton = document.getElementById("myBtn");

		mybutton.style.background = myVar;
    }	
	else
	{	// Change for div.sub_topic
        var divClass = document.getElementsByClassName("sub_topic");
		
        for(var i = 0; i < divClass.length; i++)
        {	//do something to each div
            divClass[i].style.backgroundColor = myVar;
        }
	}
}



function scrollFunction() 
{
	// Get the button
    var mybutton = document.getElementById("myBtn");

	if (document.body.scrollTop > 50 || document.documentElement.scrollTop > 50) 
	{
		mybutton.style.display = "block";
	} 
	else 
	{
		mybutton.style.display = "none";
	}
}

// When the user clicks on the button, scroll to the top of the document
function topFunction() 
{
	document.body.scrollTop = 0;
	document.documentElement.scrollTop = 0;
}

