" Author: Daniel Leong
"

function! intellivim#java#Setup() " {{{

    " define commands {{{
    if !exists(":JavaOptimizeImports")
        command -nargs=0 JavaOptimizeImports
            \ call intellivim#java#OptimizeImports()
    endif
    " }}}

endfunction " }}}

function! intellivim#java#OptimizeImports() " {{{

    let command = intellivim#NewCommand("java_import_optimize")
    let result = intellivim#client#Execute(command)

    if intellivim#ShowErrorResult(result)
        return
    endif

    call intellivim#core#ReloadFile()

    if !has_key(result, 'result') || !type(result.result) == type([])
        " all unambiguous
        return
    endif

    " fix ambiguousness
    let fixes = result.result
    if len(fixes) == 0
        " this *shouldn't* happen if we get here,
        "  but just in case....
        return
    endif

    let b:intellivim_pending_fixes = fixes
    let b:intellivim_last_fix_index = 0
    call intellivim#core#problems#PromptFix(fixes[0], {
            \ 'returnWinNr': bufwinnr('%'),
            \ 'onDone': function("s:OnContinueImportResolution")
            \ })

endfunction " }}}

"
" Callbacks
"

function! s:OnContinueImportResolution() " {{{
    let fixes = b:intellivim_pending_fixes
    let index = b:intellivim_last_fix_index + 1
    if index >= len(fixes)
        " we're done!
        unlet b:intellivim_pending_fixes
        unlet b:intellivim_last_fix_index
        return
    endif

    let b:intellivim_last_fix_index = index
    call intellivim#core#problems#PromptFix(fixes[index], {
            \ 'returnWinNr': bufwinnr('%'),
            \ 'onDone': function("s:OnContinueImportResolution")
            \ })
endfunction " }}}

" vim:ft=vim:fdm=marker
