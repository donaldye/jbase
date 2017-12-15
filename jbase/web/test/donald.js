/**
 * Created by yejaz on 11/26/2016.
 */

var isEnable = true;
var when_i_click = function(){
    alert('image clicked!');

    var eles = document.getElementById("img1");
    if (isEnable)
    {
        eles.onclick=null	;
        isEnable = false;
    }
    else
    {
        eles.onclick = when_i_click;
        isEnable = true;
    }
}
