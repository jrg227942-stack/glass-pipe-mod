Add-Type -AssemblyName System.Drawing

$base = "src/main/resources/assets/glass_pipe_transport"

function Save-Texture($bmp, $path) {
    $dir = Split-Path $path -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    $bmp.Save((Resolve-Path -LiteralPath (Split-Path $path -Parent)).Path + "\" + (Split-Path $path -Leaf), [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Host "Created: $path"
    $bmp.Dispose()
}

# --- glass_pipe.png (16x16, transparent blue glass) ---
$b = New-Object System.Drawing.Bitmap 16,16
for ($y=0;$y-lt16;$y++){for($x=0;$x-lt16;$x++){
    if($x-eq0-or$x-eq15-or$y-eq0-or$y-eq15){$b.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(200,180,220,255))}
    elseif($x-eq1-or$x-eq14-or$y-eq1-or$y-eq14){$b.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(160,200,235,255))}
    else{$b.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(40,150,200,255))}
}}
Save-Texture $b "$base/textures/block/glass_pipe.png"

# --- filter_pipe.png (16x16, orange tinted glass) ---
$b = New-Object System.Drawing.Bitmap 16,16
for ($y=0;$y-lt16;$y++){for($x=0;$x-lt16;$x++){
    if($x-eq0-or$x-eq15-or$y-eq0-or$y-eq15){$b.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(200,255,180,80))}
    elseif($x-eq1-or$x-eq14-or$y-eq1-or$y-eq14){$b.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(160,255,200,100))}
    else{$b.SetPixel($x,$y,[System.Drawing.Color]::FromArgb(40,255,160,60))}
}}
Save-Texture $b "$base/textures/block/filter_pipe.png"

# --- speed_upgrade.png (16x16, yellow bolt on blue) ---
$b = New-Object System.Drawing.Bitmap 16,16
$g = [System.Drawing.Graphics]::FromImage($b)
$g.Clear([System.Drawing.Color]::FromArgb(255,30,80,180))
$g.FillPolygon([System.Drawing.Brushes]::Yellow, @(
    [System.Drawing.Point]::new(9,1),[System.Drawing.Point]::new(12,1),
    [System.Drawing.Point]::new(8,8),[System.Drawing.Point]::new(11,8),
    [System.Drawing.Point]::new(6,15),[System.Drawing.Point]::new(4,15),
    [System.Drawing.Point]::new(8,8),[System.Drawing.Point]::new(5,8)
))
$g.Dispose()
Save-Texture $b "$base/textures/item/speed_upgrade.png"

# --- sorting_upgrade.png (16x16, white cross on green) ---
$b = New-Object System.Drawing.Bitmap 16,16
$g = [System.Drawing.Graphics]::FromImage($b)
$g.Clear([System.Drawing.Color]::FromArgb(255,30,150,60))
$g.FillRectangle([System.Drawing.Brushes]::White, 2,7,12,2)
$g.FillRectangle([System.Drawing.Brushes]::White, 7,2,2,12)
$g.FillPolygon([System.Drawing.Brushes]::White,@([System.Drawing.Point]::new(13,6),[System.Drawing.Point]::new(15,8),[System.Drawing.Point]::new(13,10)))
$g.FillPolygon([System.Drawing.Brushes]::White,@([System.Drawing.Point]::new(6,13),[System.Drawing.Point]::new(8,15),[System.Drawing.Point]::new(10,13)))
$g.Dispose()
Save-Texture $b "$base/textures/item/sorting_upgrade.png"

# --- pipe_wrench.png (16x16, grey wrench on dark) ---
$b = New-Object System.Drawing.Bitmap 16,16
$g = [System.Drawing.Graphics]::FromImage($b)
$g.Clear([System.Drawing.Color]::FromArgb(255,40,40,40))
$pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255,190,190,190)), 2.5
$g.DrawLine($pen, 3,13,13,3)
$g.DrawEllipse($pen, 1,1,6,6)
$pen.Dispose()
$g.Dispose()
Save-Texture $b "$base/textures/item/pipe_wrench.png"

# --- filter_pipe GUI (176x166) ---
$b = New-Object System.Drawing.Bitmap 176,166
$g = [System.Drawing.Graphics]::FromImage($b)
$g.Clear([System.Drawing.Color]::FromArgb(255,198,198,198))
$g.DrawRectangle([System.Drawing.Pens]::DimGray, 0,0,175,165)
$g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(180,100,130,160))), 2,2,172,68)
# filter slots 3x3
for($row=0;$row-lt3;$row++){for($col=0;$col-lt3;$col++){
    $sx=44+$col*18; $sy=17+$row*18
    $g.FillRectangle([System.Drawing.Brushes]::DimGray,$sx-1,$sy-1,18,18)
    $g.FillRectangle([System.Drawing.Brushes]::LightGray,$sx,$sy,16,16)
}}
# player inv slots
for($row=0;$row-lt3;$row++){for($col=0;$col-lt9;$col++){
    $sx=8+$col*18; $sy=84+$row*18
    $g.FillRectangle([System.Drawing.Brushes]::DimGray,$sx-1,$sy-1,18,18)
    $g.FillRectangle([System.Drawing.Brushes]::LightGray,$sx,$sy,16,16)
}}
# hotbar
for($col=0;$col-lt9;$col++){
    $sx=8+$col*18; $sy=142
    $g.FillRectangle([System.Drawing.Brushes]::DimGray,$sx-1,$sy-1,18,18)
    $g.FillRectangle([System.Drawing.Brushes]::LightGray,$sx,$sy,16,16)
}
$g.Dispose()
Save-Texture $b "$base/textures/gui/filter_pipe.png"

# --- icon.png (64x64) ---
$b = New-Object System.Drawing.Bitmap 64,64
$g = [System.Drawing.Graphics]::FromImage($b)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.Clear([System.Drawing.Color]::FromArgb(255,20,40,80))
$g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(200,150,210,255))),24,8,16,48)
$g.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(200,150,210,255))),8,24,48,16)
$g.FillEllipse((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(200,255,255,255))),26,26,12,12)
$g.Dispose()
Save-Texture $b "$base/icon.png"

Write-Host "`nAll textures generated successfully!"
