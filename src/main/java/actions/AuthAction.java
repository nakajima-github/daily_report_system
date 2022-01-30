package actions;

import java.io.IOException;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.MessageConst;
import constants.PropertyConst;
import services.EmployeeService;

public class AuthAction extends ActionBase {
    private EmployeeService service;

    /**
     * メソッドの実行
     */
    @Override
    public void process() throws ServletException, IOException {
        service = new EmployeeService();
        invoke();
        service.close();

    }

    /**
     * ログイン画面を表示
     * @throws ServletException
     * @throws IOException
     */
    public void showLogin() throws ServletException, IOException {

        putRequestScope(AttributeConst.TOKEN, getTokenId());

        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }
        forward(ForwardConst.FW_LOGIN);
    }

    /**
     * ログイン処理
     * @throws ServletException
     * @throws IOException
     */
    public void login() throws ServletException, IOException {
        String code = getRequestParam(AttributeConst.EMP_CODE);
        String plainPass = getRequestParam(AttributeConst.EMP_PASS);
        String pepper = getContextScope(PropertyConst.PEPPER);

        Boolean isValidEmployee = service.validateLogin(code, plainPass, pepper);
        if (isValidEmployee) {
            if (checkToken()) {
                // ログインした従業員のデータを取得
                EmployeeView ev = service.findOne(code, plainPass, pepper);
                // セッションにログインした従業員情報を設定
                putSessionScope(AttributeConst.LOGIN_EMP, ev);
                // セッションにログイン完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_LOGINED.getMessage());
                // トップページへリダイレクト
                redirect(ForwardConst.ACT_TOP, ForwardConst.CMD_INDEX);

            }
        } else {
            // CSRF対策用トークンを設定
            putRequestScope(AttributeConst.TOKEN, getTokenId());
            // 認証失敗のエラーメッセージ表示フラグを設定
            putRequestScope(AttributeConst.LOGIN_ERR, true);
            // 入力された従業員コードを設定
            putRequestScope(AttributeConst.EMP_CODE, code);

            // ログイン画面を表示
            forward(ForwardConst.FW_LOGIN);
        }

    }
}
