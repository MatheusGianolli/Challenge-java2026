/* =====================================================================
 * Clyvo PetCare — interface web (HTML + JavaScript puro)
 *
 * Servida pelo próprio Spring Boot (src/main/resources/static) e
 * consumindo a API REST com autenticação HTTP Basic.
 * ===================================================================== */

// ------------------------------------------------------------------
// Estado e utilitários
// ------------------------------------------------------------------
const estado = { credencial: null, usuario: null, perfis: [] };
const $ = (seletor) => document.querySelector(seletor);
const conteudo = $("#conteudo");

const ehAdmin = () => estado.perfis.includes("ADMIN");

function esc(valor) {
    return String(valor ?? "").replace(/[&<>"']/g, (c) =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

const moeda = (v) => (v == null ? "—" : Number(v).toLocaleString("pt-BR", { style: "currency", currency: "BRL" }));

function dataHora(v) {
    if (!v) return "—";
    const d = new Date(v);
    return isNaN(d) ? esc(v) : d.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

function data(v) {
    if (!v) return "—";
    const [a, m, d] = String(v).substring(0, 10).split("-");
    return `${d}/${m}/${a}`;
}

function etiqueta(texto) {
    const t = String(texto ?? "").toUpperCase();
    const tipo = { ATIVO: "ok", REALIZADA: "ok", AGENDADA: "alerta", INATIVO: "neutra", CANCELADA: "perigo", S: "ok", N: "neutra" }[t] || "neutra";
    const rotulo = { S: "Sim", N: "Não" }[t] || texto || "—";
    return `<span class="etiqueta etiqueta-${tipo}">${esc(rotulo)}</span>`;
}

function aviso(mensagem, tipo = "") {
    const el = document.createElement("div");
    el.className = `aviso ${tipo ? "aviso-" + tipo : ""}`;
    el.textContent = mensagem;
    $("#avisos").appendChild(el);
    setTimeout(() => el.remove(), 5000);
}

function guardar(chave, valor) {
    try {
        valor == null ? sessionStorage.removeItem(chave) : sessionStorage.setItem(chave, valor);
    } catch (e) { /* armazenamento indisponível: segue só em memória */ }
}

function ler(chave) {
    try { return sessionStorage.getItem(chave); } catch (e) { return null; }
}

// ------------------------------------------------------------------
// Acesso à API
// ------------------------------------------------------------------
class ErroApi extends Error {
    constructor(status, mensagem) { super(mensagem); this.status = status; }
}

async function api(caminho, { metodo = "GET", corpo, bruto = false } = {}) {
    const resposta = await fetch(caminho, {
        method: metodo,
        headers: {
            "Authorization": "Basic " + estado.credencial,
            // Evita a janela de login nativa do navegador em respostas 401
            "X-Requested-With": "XMLHttpRequest",
            ...(corpo !== undefined ? { "Content-Type": "application/json" } : {})
        },
        body: corpo !== undefined ? JSON.stringify(corpo) : undefined
    });

    if (resposta.status === 401) {
        sair();
        throw new ErroApi(401, "Sessão expirada. Entre novamente.");
    }
    if (resposta.status === 403) {
        throw new ErroApi(403, "Seu perfil não tem permissão para esta ação.");
    }
    if (!resposta.ok) {
        let mensagem = `Erro ${resposta.status}`;
        try {
            const json = await resposta.json();
            if (json.errors) mensagem = Object.values(json.errors).join(" · ");
            else if (json.error) mensagem = json.error;
            else if (json.message) mensagem = json.message;
        } catch (e) { /* corpo sem JSON */ }
        throw new ErroApi(resposta.status, mensagem);
    }
    if (bruto) return resposta;
    if (resposta.status === 204) return null;
    const texto = await resposta.text();
    return texto ? JSON.parse(texto) : null;
}

// Listagens paginadas: busca uma página grande e devolve só o conteúdo
async function listar(recurso, ordem) {
    const pagina = await api(`/api/${recurso}?size=200${ordem ? "&sort=" + ordem : ""}`);
    return pagina?.content ?? [];
}

async function executar(acao, mensagemOk) {
    try {
        const r = await acao();
        if (mensagemOk) aviso(mensagemOk, "ok");
        return r;
    } catch (e) {
        aviso(e.message, "erro");
        throw e;
    }
}

// ------------------------------------------------------------------
// Login / logout
// ------------------------------------------------------------------
async function entrar(usuario, senha) {
    estado.credencial = btoa(unescape(encodeURIComponent(`${usuario}:${senha}`)));
    const eu = await api("/api/me");
    estado.usuario = eu.usuario;
    estado.perfis = eu.perfis;
    guardar("clyvo.credencial", estado.credencial);

    $("#usuario-nome").textContent = eu.usuario;
    $("#usuario-perfil").textContent = ehAdmin() ? "Administrador" : "Veterinário";
    $("#tela-login").hidden = true;
    $("#tela-app").hidden = false;
    navegar();
}

function sair() {
    estado.credencial = null;
    estado.usuario = null;
    estado.perfis = [];
    guardar("clyvo.credencial", null);
    $("#tela-app").hidden = true;
    $("#tela-login").hidden = false;
}

$("#form-login").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    const erro = $("#login-erro");
    erro.hidden = true;
    try {
        await entrar($("#login-usuario").value.trim(), $("#login-senha").value);
    } catch (e) {
        erro.textContent = e.status === 401 || e.status === 403 ? "Usuário ou senha inválidos." : e.message;
        erro.hidden = false;
    }
});

$("#btn-sair").addEventListener("click", sair);

// ------------------------------------------------------------------
// Modal genérico de formulário
// ------------------------------------------------------------------
const modal = $("#modal");
let aoSalvarModal = null;

function abrirModal(titulo, html, aoSalvar, rotuloSalvar = "Salvar") {
    $("#modal-titulo").textContent = titulo;
    $("#modal-corpo").innerHTML = html;
    $("#modal-salvar").textContent = rotuloSalvar;
    // Modal só de leitura (ex.: prontuário): mostra apenas o botão "Fechar"
    modal.querySelector(".modal-rodape [data-fechar]").hidden = !aoSalvar;
    aoSalvarModal = aoSalvar;
    modal.showModal();
    modal.querySelector("input, select, textarea")?.focus();
}

modal.querySelectorAll("[data-fechar]").forEach((b) => b.addEventListener("click", () => modal.close()));

$("#modal-form").addEventListener("submit", async (ev) => {
    ev.preventDefault();
    if (!aoSalvarModal) return modal.close();
    const botao = $("#modal-salvar");
    botao.disabled = true;
    try {
        await aoSalvarModal(new FormData(ev.target));
        modal.close();
    } catch (e) {
        /* o aviso de erro já foi exibido; o modal continua aberto para correção */
    } finally {
        botao.disabled = false;
    }
});

// Monta os campos de um formulário a partir de uma lista de definições
function camposHtml(campos, valores = {}) {
    return `<div class="grade-form">${campos.map((c) => {
        const v = valores[c.nome] ?? c.padrao ?? "";
        const classe = c.inteiro ? "inteiro" : "";
        const req = c.obrigatorio ? "required" : "";
        const rotulo = `<span class="${c.obrigatorio ? "obrigatorio" : ""}">${esc(c.rotulo)}</span>`;

        if (c.tipo === "select") {
            const opcoes = [`<option value="">Selecione…</option>`]
                .concat(c.opcoes.map((o) => {
                    const [valor, texto] = Array.isArray(o) ? o : [o, o];
                    return `<option value="${esc(valor)}" ${String(valor) === String(v) ? "selected" : ""}>${esc(texto)}</option>`;
                })).join("");
            return `<label class="${classe}">${rotulo}<select name="${c.nome}" ${req}>${opcoes}</select></label>`;
        }
        if (c.tipo === "textarea") {
            return `<label class="${classe}">${rotulo}<textarea name="${c.nome}" ${req}>${esc(v)}</textarea></label>`;
        }
        const extras = [
            c.max ? `maxlength="${c.max}"` : "",
            c.passo ? `step="${c.passo}" min="0"` : "",
            c.placeholder ? `placeholder="${esc(c.placeholder)}"` : ""
        ].join(" ");
        return `<label class="${classe}">${rotulo}<input name="${c.nome}" type="${c.tipo || "text"}" value="${esc(v)}" ${req} ${extras}></label>`;
    }).join("")}</div>`;
}

// Converte o FormData em objeto, respeitando os tipos dos campos
function dadosDoForm(form, campos) {
    const obj = {};
    for (const c of campos) {
        const bruto = form.get(c.nome);
        const valor = bruto == null ? "" : String(bruto).trim();
        if (valor === "") { obj[c.nome] = null; continue; }
        obj[c.nome] = c.tipo === "number" || c.numerico ? Number(valor) : valor;
    }
    return obj;
}

// ------------------------------------------------------------------
// Tela genérica de cadastro (lista + incluir + editar + excluir)
// ------------------------------------------------------------------
async function telaCadastro(cfg) {
    conteudo.innerHTML = `
        <div class="cabecalho">
            <div><h1>${esc(cfg.titulo)}</h1><p>${esc(cfg.descricao)}</p></div>
            <div class="acoes">
                <input class="busca" id="busca" type="search" placeholder="Buscar…">
                ${cfg.podeCriar() ? `<button class="btn btn-primario" id="btn-novo">+ Novo</button>` : ""}
            </div>
        </div>
        <div class="cartao tabela-wrap"><table>
            <thead><tr>${cfg.colunas.map((c) => `<th class="${c.num ? "num" : ""}">${esc(c.rotulo)}</th>`).join("")}<th></th></tr></thead>
            <tbody id="linhas"><tr><td class="vazio" colspan="99">Carregando…</td></tr></tbody>
        </table></div>`;

    let itens = [];

    const desenhar = () => {
        const termo = $("#busca").value.toLowerCase();
        const visiveis = itens.filter((i) => !termo || JSON.stringify(i).toLowerCase().includes(termo));
        $("#linhas").innerHTML = visiveis.length ? visiveis.map((i) => `
            <tr>
                ${cfg.colunas.map((c) => `<td class="${c.num ? "num" : ""}">${c.formato ? c.formato(i[c.campo], i) : esc(i[c.campo] ?? "—")}</td>`).join("")}
                <td class="acoes-linha">
                    ${(cfg.acoesExtras?.(i) ?? []).map((a, n) => `<button class="btn btn-pequeno" data-extra="${n}" data-id="${i.id}">${esc(a.rotulo)}</button>`).join("")}
                    ${cfg.podeEditar() ? `<button class="btn btn-pequeno" data-editar="${i.id}">Editar</button>` : ""}
                    ${cfg.podeExcluir() ? `<button class="btn btn-pequeno btn-perigo" data-excluir="${i.id}">Excluir</button>` : ""}
                </td>
            </tr>`).join("") : `<tr><td class="vazio" colspan="99">Nenhum registro encontrado.</td></tr>`;
    };

    const recarregar = async () => {
        try {
            itens = await listar(cfg.recurso, cfg.ordem);
            desenhar();
        } catch (e) {
            $("#linhas").innerHTML = `<tr><td class="vazio" colspan="99">${esc(e.message)}</td></tr>`;
        }
    };

    const abrirForm = async (item) => {
        const campos = typeof cfg.campos === "function" ? await cfg.campos() : cfg.campos;
        const valores = item ? (cfg.paraForm ? cfg.paraForm(item) : item) : {};
        abrirModal(item ? `Editar ${cfg.singular}` : `Novo ${cfg.singular}`, camposHtml(campos, valores), async (form) => {
            const corpo = dadosDoForm(form, campos);
            await executar(
                () => api(`/api/${cfg.recurso}${item ? "/" + item.id : ""}`, { metodo: item ? "PUT" : "POST", corpo }),
                item ? `${cfg.singular} atualizado.` : `${cfg.singular} cadastrado.`
            );
            await recarregar();
        });
    };

    $("#busca").addEventListener("input", desenhar);
    $("#btn-novo")?.addEventListener("click", () => abrirForm());

    $("#linhas").addEventListener("click", async (ev) => {
        const b = ev.target.closest("button");
        if (!b) return;
        const item = itens.find((i) => String(i.id) === (b.dataset.editar || b.dataset.excluir || b.dataset.id));
        if (b.dataset.editar) return abrirForm(item);
        if (b.dataset.excluir) {
            if (!confirm(`Excluir ${cfg.singular.toLowerCase()} "${item.nome ?? item.id}"?`)) return;
            await executar(() => api(`/api/${cfg.recurso}/${item.id}`, { metodo: "DELETE" }), "Registro excluído.").catch(() => {});
            return recarregar();
        }
        if (b.dataset.extra != null) {
            await cfg.acoesExtras(item)[Number(b.dataset.extra)].acao(item).catch(() => {});
            return recarregar();
        }
    });

    await recarregar();
}

// ------------------------------------------------------------------
// Definições das telas de cadastro
// ------------------------------------------------------------------
const SIM_NAO = [["S", "Sim"], ["N", "Não"]];
const UFS = ["AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG","PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO"];

const camposEndereco = [
    { nome: "logradouro", rotulo: "Logradouro" },
    { nome: "numeroEndereco", rotulo: "Número" },
    { nome: "bairro", rotulo: "Bairro" },
    { nome: "cidade", rotulo: "Cidade" },
    { nome: "estado", rotulo: "UF", tipo: "select", opcoes: UFS },
    { nome: "cep", rotulo: "CEP", placeholder: "00000-000" }
];

const telas = {
    tutores: () => telaCadastro({
        titulo: "Tutores", singular: "Tutor", recurso: "tutores", ordem: "nome",
        descricao: "Responsáveis pelos pets: tutores, ONGs e abrigos.",
        podeCriar: () => true, podeEditar: () => true, podeExcluir: ehAdmin,
        colunas: [
            { campo: "nome", rotulo: "Nome" },
            { campo: "numeroDocumento", rotulo: "Documento" },
            { campo: "email", rotulo: "E-mail" },
            { campo: "telefone", rotulo: "Telefone" },
            { campo: "cidade", rotulo: "Cidade" },
            { campo: "status", rotulo: "Status", formato: etiqueta }
        ],
        campos: [
            { nome: "nome", rotulo: "Nome", obrigatorio: true, inteiro: true },
            { nome: "tipoDocumento", rotulo: "Tipo de documento", tipo: "select", opcoes: ["CPF", "CNPJ"], padrao: "CPF" },
            { nome: "numeroDocumento", rotulo: "Documento", obrigatorio: true },
            { nome: "email", rotulo: "E-mail", tipo: "email", obrigatorio: true },
            { nome: "telefone", rotulo: "Telefone", obrigatorio: true },
            { nome: "dataNascimento", rotulo: "Data de nascimento", tipo: "date" },
            { nome: "tipoResponsavel", rotulo: "Tipo de responsável", tipo: "select", opcoes: ["TUTOR", "ONG", "ABRIGO"], padrao: "TUTOR" },
            { nome: "preferenciaContato", rotulo: "Preferência de contato", tipo: "select", opcoes: ["WHATSAPP", "EMAIL", "TELEFONE"] },
            { nome: "recebeNotificacao", rotulo: "Recebe notificações", tipo: "select", opcoes: SIM_NAO, padrao: "S" },
            { nome: "autorizaMonitoramento", rotulo: "Autoriza monitoramento", tipo: "select", opcoes: SIM_NAO, padrao: "S" },
            ...camposEndereco
        ]
    }),

    pets: () => telaCadastro({
        titulo: "Pets", singular: "Pet", recurso: "pets", ordem: "nome",
        descricao: "Animais acompanhados pela plataforma. Abra o prontuário para ver todo o histórico de saúde.",
        podeCriar: () => true, podeEditar: () => true, podeExcluir: ehAdmin,
        acoesExtras: () => [{ rotulo: "🩺 Prontuário", acao: (pet) => abrirProntuario(pet.id) }],
        colunas: [
            { campo: "nome", rotulo: "Nome" },
            { campo: "especie", rotulo: "Espécie" },
            { campo: "raca", rotulo: "Raça" },
            { campo: "nomeResponsavel", rotulo: "Responsável" },
            { campo: "sexo", rotulo: "Sexo", formato: (v) => ({ M: "Macho", F: "Fêmea" }[v] || "—") },
            { campo: "statusSaude", rotulo: "Saúde" },
            { campo: "alergias", rotulo: "Alergias" }
        ],
        campos: async () => {
            const tutores = await listar("tutores", "nome");
            return [
                { nome: "responsavelId", rotulo: "Responsável", tipo: "select", obrigatorio: true, inteiro: true, numerico: true,
                  opcoes: tutores.map((t) => [t.id, `${t.nome} (${t.numeroDocumento})`]) },
                { nome: "nome", rotulo: "Nome", obrigatorio: true },
                { nome: "especie", rotulo: "Espécie", tipo: "select", obrigatorio: true, opcoes: ["Cachorro", "Gato", "Ave", "Roedor", "Réptil", "Outro"] },
                { nome: "raca", rotulo: "Raça" },
                { nome: "sexo", rotulo: "Sexo", tipo: "select", obrigatorio: true, opcoes: [["M", "Macho"], ["F", "Fêmea"]] },
                { nome: "dataNascimento", rotulo: "Data de nascimento", tipo: "date" },
                { nome: "porte", rotulo: "Porte", tipo: "select", opcoes: ["PEQUENO", "MEDIO", "GRANDE"] },
                { nome: "peso", rotulo: "Peso (kg)", tipo: "number", passo: "0.1" },
                { nome: "altura", rotulo: "Altura (m)", tipo: "number", passo: "0.01" },
                { nome: "castrado", rotulo: "Castrado", tipo: "select", opcoes: SIM_NAO },
                { nome: "statusSaude", rotulo: "Status de saúde", tipo: "select", opcoes: ["SAUDAVEL", "EM OBSERVACAO", "EM TRATAMENTO"] },
                { nome: "corPelagem", rotulo: "Cor da pelagem" },
                { nome: "microchip", rotulo: "Microchip" },
                { nome: "alergias", rotulo: "Alergias", tipo: "textarea", inteiro: true }
            ];
        }
    }),

    clinicas: () => telaCadastro({
        titulo: "Clínicas", singular: "Clínica", recurso: "clinicas", ordem: "nome",
        descricao: "Clínicas e hospitais veterinários parceiros.",
        podeCriar: () => true, podeEditar: () => true, podeExcluir: ehAdmin,
        colunas: [
            { campo: "nome", rotulo: "Nome" },
            { campo: "cnpj", rotulo: "CNPJ" },
            { campo: "cidade", rotulo: "Cidade" },
            { campo: "telefone", rotulo: "Telefone" },
            { campo: "atendimento24h", rotulo: "24h", formato: etiqueta },
            { campo: "status", rotulo: "Status", formato: etiqueta }
        ],
        acoesExtras: (c) => [{
            rotulo: c.status === "ATIVO" ? "Desativar" : "Ativar",
            acao: (item) => executar(() => api(`/api/clinicas/${item.id}/status`, {
                metodo: "PATCH", corpo: { status: item.status === "ATIVO" ? "INATIVO" : "ATIVO" }
            }), "Status da clínica alterado.")
        }],
        campos: [
            { nome: "nome", rotulo: "Nome", obrigatorio: true, inteiro: true },
            { nome: "cnpj", rotulo: "CNPJ", obrigatorio: true, placeholder: "00.000.000/0000-00" },
            { nome: "email", rotulo: "E-mail", tipo: "email", obrigatorio: true },
            { nome: "telefone", rotulo: "Telefone", obrigatorio: true },
            { nome: "tipoEstabelecimento", rotulo: "Tipo", tipo: "select", opcoes: ["CLINICA VETERINARIA", "HOSPITAL VETERINARIO"] },
            { nome: "especialidadePrincipal", rotulo: "Especialidade principal" },
            { nome: "atendimento24h", rotulo: "Atendimento 24h", tipo: "select", opcoes: SIM_NAO, padrao: "N" },
            { nome: "atendeEmergencia", rotulo: "Atende emergência", tipo: "select", opcoes: SIM_NAO, padrao: "N" },
            ...camposEndereco
        ]
    }),

    veterinarios: () => telaCadastro({
        titulo: "Veterinários", singular: "Veterinário", recurso: "veterinarios", ordem: "nome",
        descricao: "Profissionais que realizam os atendimentos. Cadastro e edição exclusivos do administrador.",
        podeCriar: ehAdmin, podeEditar: ehAdmin, podeExcluir: ehAdmin,
        colunas: [
            { campo: "nome", rotulo: "Nome" },
            { campo: "crmv", rotulo: "CRMV" },
            { campo: "especialidade", rotulo: "Especialidade" },
            { campo: "tipoAtuacao", rotulo: "Atuação" },
            { campo: "telefone", rotulo: "Telefone" },
            { campo: "status", rotulo: "Status", formato: etiqueta }
        ],
        acoesExtras: (v) => ehAdmin() ? [{
            rotulo: v.status === "ATIVO" ? "Desativar" : "Ativar",
            acao: (item) => executar(() => api(
                `/api/veterinarios/${item.id}/status?status=${item.status === "ATIVO" ? "INATIVO" : "ATIVO"}`,
                { metodo: "PATCH" }), "Status do veterinário alterado.")
        }] : [],
        campos: [
            { nome: "nome", rotulo: "Nome", obrigatorio: true, inteiro: true },
            { nome: "crmv", rotulo: "CRMV", obrigatorio: true, placeholder: "CRMV-SP 00000" },
            { nome: "especialidade", rotulo: "Especialidade" },
            { nome: "tipoAtuacao", rotulo: "Tipo de atuação", tipo: "select", opcoes: ["CLINICA", "AUTONOMO", "HIBRIDO"] },
            { nome: "email", rotulo: "E-mail", tipo: "email", obrigatorio: true },
            { nome: "telefone", rotulo: "Telefone", obrigatorio: true }
        ]
    }),

    painel: telaPainel,
    consultas: telaConsultas,
    relatorios: telaRelatorios
};

// ------------------------------------------------------------------
// Prontuário do pet (histórico completo de saúde)
// ------------------------------------------------------------------
async function abrirProntuario(petId) {
    let h;
    try {
        h = await api(`/api/pets/${petId}/historico`);
    } catch (e) {
        aviso(e.message, "erro");
        return;
    }
    const p = h.pet;
    const idade = p.dataNascimento
        ? Math.floor((Date.now() - new Date(p.dataNascimento)) / (365.25 * 24 * 3600 * 1000)) + " ano(s)"
        : "—";

    const linhaDoTempo = h.consultas.length ? h.consultas.map((c) => `
        <div class="evento evento-${String(c.status).toLowerCase()}">
            <div class="evento-data">${dataHora(c.dataPrevista)} ${etiqueta(c.status)}</div>
            <div><strong>${esc(c.tipoConsulta)}</strong> · ${esc(c.nomeVeterinario)} · ${esc(c.nomeClinica)}</div>
            ${c.descricaoSintomas ? `<div class="evento-texto">Motivo: ${esc(c.descricaoSintomas)}</div>` : ""}
            ${c.diagnostico ? `<div class="evento-texto">Diagnóstico: ${esc(c.diagnostico)}</div>` : ""}
            ${c.retornoPrevisto ? `<div class="evento-texto">Retorno previsto: ${dataHora(c.retornoPrevisto)}</div>` : ""}
        </div>`).join("") : `<p class="vazio">Nenhum atendimento registrado.</p>`;

    abrirModal(`Prontuário — ${p.nome}`, `
        <div class="indicadores">
            <div class="cartao indicador"><span>Atendimentos</span><strong>${h.consultasRealizadas}</strong></div>
            <div class="cartao indicador"><span>Último atendimento</span><strong style="font-size:15px">${dataHora(h.ultimoAtendimento)}</strong></div>
            <div class="cartao indicador"><span>Próxima consulta</span><strong style="font-size:15px">${dataHora(h.proximaConsulta)}</strong></div>
            <div class="cartao indicador"><span>Próximo retorno</span><strong style="font-size:15px">${dataHora(h.proximoRetorno)}</strong></div>
        </div>
        <div class="grade-form">
            <div><strong>Espécie / raça</strong><br>${esc(p.especie)} · ${esc(p.raca || "—")}</div>
            <div><strong>Idade</strong><br>${idade}</div>
            <div><strong>Peso / porte</strong><br>${p.peso != null ? esc(p.peso) + " kg" : "—"} · ${esc(p.porte || "—")}</div>
            <div><strong>Status de saúde</strong><br>${esc(p.statusSaude || "—")}</div>
            <div class="inteiro ${p.alergias ? "alerta-alergia" : ""}"><strong>Alergias</strong><br>${esc(p.alergias || "Nenhuma registrada")}</div>
            <div><strong>Responsável</strong><br>${esc(p.nomeResponsavel)}</div>
            <div><strong>Contato</strong><br>${esc(h.telefoneResponsavel || "—")} · ${esc(h.emailResponsavel || "")}</div>
        </div>
        <h3 style="font-size:15px">Linha do tempo</h3>
        <div class="linha-tempo">${linhaDoTempo}</div>`, null, "Fechar");
}

// ------------------------------------------------------------------
// Painel
// ------------------------------------------------------------------
async function telaPainel() {
    conteudo.innerHTML = `
        <div class="cabecalho"><div><h1>Olá, ${esc(estado.usuario)} 👋</h1>
            <p>Acompanhamento contínuo da saúde dos pets: agenda, retornos e histórico.</p></div></div>
        <div class="indicadores" id="indicadores">${"<div class='cartao indicador'><span>…</span><strong>–</strong></div>".repeat(5)}</div>
        <div class="duas-colunas">
            <div class="cartao"><h3>Próximas consultas</h3><div class="tabela-wrap" id="proximas">Carregando…</div></div>
            <div class="cartao"><h3>Faturamento por veterinário</h3><div class="tabela-wrap" id="faturamento">Carregando…</div>
                <p style="color:var(--cor-suave);font-size:12px;margin:10px 0 0">Calculado pela procedure <code>pkg_clyvo.resumo_consultas</code> no Oracle.</p></div>
        </div>
        <div class="cartao"><h3>🔔 Retornos previstos nos próximos 30 dias</h3><div class="tabela-wrap" id="retornos">Carregando…</div></div>`;

    try {
        const [tutores, pets, clinicas, vets, consultas, resumo, retornos] = await Promise.all([
            listar("tutores"), listar("pets"), listar("clinicas"), listar("veterinarios"),
            listar("consultas", "dataPrevista,asc"), api("/api/relatorios/resumo-consultas"),
            api("/api/consultas/retornos?dias=30")
        ]);
        const agendadas = consultas.filter((c) => c.status === "AGENDADA");
        const total = resumo.find((l) => l.tipoLinha === "TOTAL_GERAL");

        $("#indicadores").innerHTML = [
            ["Tutores", tutores.length], ["Pets", pets.length],
            ["Clínicas ativas", clinicas.filter((c) => c.status === "ATIVO").length],
            ["Consultas agendadas", agendadas.length], ["Faturamento", moeda(total?.valorTotal ?? 0)]
        ].map(([r, v]) => `<div class="cartao indicador"><span>${r}</span><strong>${esc(v)}</strong></div>`).join("");

        $("#proximas").innerHTML = agendadas.length ? `<table><thead><tr><th>Data</th><th>Pet</th><th>Tipo</th><th>Veterinário</th></tr></thead><tbody>
            ${agendadas.slice(0, 6).map((c) => `<tr><td>${dataHora(c.dataPrevista)}</td><td>${esc(c.nomePet)}</td><td>${esc(c.tipoConsulta)}</td><td>${esc(c.nomeVeterinario)}</td></tr>`).join("")}
            </tbody></table>` : `<p class="vazio">Nenhuma consulta agendada.</p>`;

        $("#retornos").innerHTML = retornos.length ? `<table><thead><tr><th>Retorno</th><th>Pet</th><th>Responsável</th><th>Diagnóstico anterior</th><th>Veterinário</th></tr></thead><tbody>
            ${retornos.map((c) => `<tr><td>${dataHora(c.retornoPrevisto)}</td><td>${esc(c.nomePet)}</td><td>${esc(c.nomeResponsavel)}</td><td>${esc(c.diagnostico || "—")}</td><td>${esc(c.nomeVeterinario)}</td></tr>`).join("")}
            </tbody></table>` : `<p class="vazio">Nenhum retorno previsto para os próximos 30 dias.</p>`;

        const subtotais = resumo.filter((l) => l.tipoLinha === "SUBTOTAL");
        $("#faturamento").innerHTML = subtotais.length ? `<table><thead><tr><th>Veterinário</th><th class="num">Consultas</th><th class="num">Total</th></tr></thead><tbody>
            ${subtotais.map((l) => `<tr><td>${esc(l.veterinario)}</td><td class="num">${l.quantidade}</td><td class="num">${moeda(l.valorTotal)}</td></tr>`).join("")}
            </tbody></table>` : `<p class="vazio">Sem consultas realizadas.</p>`;
    } catch (e) {
        aviso(e.message, "erro");
    }
}

// ------------------------------------------------------------------
// Consultas
// ------------------------------------------------------------------
async function telaConsultas() {
    conteudo.innerHTML = `
        <div class="cabecalho">
            <div><h1>Consultas</h1><p>Agendamentos validados pela procedure <code>pkg_clyvo.agendar_consulta</code>.</p></div>
            <div class="acoes">
                <select id="filtro-status" class="busca">
                    <option value="">Todos os status</option><option>AGENDADA</option><option>REALIZADA</option><option>CANCELADA</option>
                </select>
                <button class="btn btn-primario" id="btn-agendar">+ Agendar consulta</button>
            </div>
        </div>
        <div class="cartao tabela-wrap"><table>
            <thead><tr><th>Data</th><th>Pet</th><th>Responsável</th><th>Tipo</th><th>Veterinário</th><th>Clínica</th><th class="num">Valor</th><th>Status</th><th></th></tr></thead>
            <tbody id="linhas"><tr><td class="vazio" colspan="99">Carregando…</td></tr></tbody>
        </table></div>`;

    let consultas = [];

    const desenhar = () => {
        const filtro = $("#filtro-status").value;
        const lista = consultas.filter((c) => !filtro || c.status === filtro);
        $("#linhas").innerHTML = lista.length ? lista.map((c) => `
            <tr>
                <td>${dataHora(c.dataPrevista)}</td>
                <td>${esc(c.nomePet)}</td>
                <td>${esc(c.nomeResponsavel)}</td>
                <td>${esc(c.tipoConsulta)}</td>
                <td>${esc(c.nomeVeterinario)}</td>
                <td>${esc(c.nomeClinica)}</td>
                <td class="num">${moeda(c.valor)}</td>
                <td>${etiqueta(c.status)}</td>
                <td class="acoes-linha">
                    <button class="btn btn-pequeno" data-ver="${c.id}">Detalhes</button>
                    ${c.status === "AGENDADA" ? `<button class="btn btn-pequeno" data-diagnostico="${c.id}">Atender</button>` : ""}
                    ${c.status === "AGENDADA" && ehAdmin() ? `<button class="btn btn-pequeno btn-perigo" data-cancelar="${c.id}">Cancelar</button>` : ""}
                </td>
            </tr>`).join("") : `<tr><td class="vazio" colspan="99">Nenhuma consulta encontrada.</td></tr>`;
    };

    const recarregar = async () => {
        try {
            consultas = await listar("consultas", "dataPrevista,desc");
            desenhar();
        } catch (e) {
            $("#linhas").innerHTML = `<tr><td class="vazio" colspan="99">${esc(e.message)}</td></tr>`;
        }
    };

    $("#filtro-status").addEventListener("change", desenhar);
    $("#btn-agendar").addEventListener("click", () => abrirAgendamento(recarregar));

    $("#linhas").addEventListener("click", async (ev) => {
        const b = ev.target.closest("button");
        if (!b) return;
        const id = b.dataset.ver || b.dataset.diagnostico || b.dataset.cancelar;
        const c = consultas.find((x) => String(x.id) === id);

        if (b.dataset.ver) {
            abrirModal(`Consulta #${c.id}`, `
                <div class="grade-form">
                    <div><strong>Pet</strong><br>${esc(c.nomePet)}</div>
                    <div><strong>Responsável</strong><br>${esc(c.nomeResponsavel)}</div>
                    <div><strong>Data</strong><br>${dataHora(c.dataPrevista)}</div>
                    <div><strong>Status</strong><br>${etiqueta(c.status)}</div>
                    <div><strong>Veterinário</strong><br>${esc(c.nomeVeterinario)}</div>
                    <div><strong>Clínica</strong><br>${esc(c.nomeClinica)}</div>
                    <div><strong>Tipo</strong><br>${esc(c.tipoConsulta)}</div>
                    <div><strong>Valor</strong><br>${moeda(c.valor)}</div>
                    <div class="inteiro"><strong>Sintomas</strong><br>${esc(c.descricaoSintomas || "—")}</div>
                    <div class="inteiro"><strong>Diagnóstico</strong><br>${esc(c.diagnostico || "—")}</div>
                    <div><strong>Retorno previsto</strong><br>${dataHora(c.retornoPrevisto)}</div>
                </div>`, null, "Fechar");
            return;
        }

        if (b.dataset.diagnostico) {
            abrirModal(`Registrar atendimento — ${c.nomePet}`, `
                <label><span class="obrigatorio">Diagnóstico</span><textarea name="diagnostico" required></textarea></label>
                <label>Retorno previsto (opcional)<input type="datetime-local" name="retorno"></label>`,
                async (form) => {
                    const params = new URLSearchParams({ diagnostico: form.get("diagnostico") });
                    const retorno = form.get("retorno");
                    if (retorno) params.set("retorno", retorno.length === 16 ? retorno + ":00" : retorno);
                    await executar(() => api(`/api/consultas/${c.id}/diagnostico?${params}`, { metodo: "PUT" }), "Atendimento registrado.");
                    await recarregar();
                }, "Finalizar consulta");
            return;
        }

        if (b.dataset.cancelar) {
            if (!confirm(`Cancelar a consulta de ${c.nomePet}?`)) return;
            await executar(() => api(`/api/consultas/${c.id}`, { metodo: "DELETE" }), "Consulta cancelada.").catch(() => {});
            await recarregar();
        }
    });

    await recarregar();
}

async function abrirAgendamento(aoConcluir) {
    let tutores, pets, clinicas, vets;
    try {
        [tutores, pets, clinicas, vets] = await Promise.all([
            listar("tutores", "nome"), listar("pets", "nome"), listar("clinicas", "nome"), listar("veterinarios", "nome")
        ]);
    } catch (e) {
        return aviso(e.message, "erro");
    }
    const opcoes = (lista, texto) => lista.map((x) => `<option value="${x.id}">${esc(texto(x))}</option>`).join("");

    abrirModal("Agendar consulta", `
        <div class="grade-form">
            <label class="inteiro"><span class="obrigatorio">Responsável</span>
                <select name="responsavelId" id="ag-tutor" required><option value="">Selecione…</option>${opcoes(tutores, (t) => t.nome)}</select></label>
            <label><span class="obrigatorio">Pet</span>
                <select name="petId" id="ag-pet" required><option value="">Escolha o responsável</option></select></label>
            <label><span class="obrigatorio">Tipo</span>
                <select name="tipoConsulta" required><option value="">Selecione…</option>
                ${["Rotina", "Vacinação", "Retorno", "Emergência", "Cirurgia", "Exame"].map((t) => `<option>${t}</option>`).join("")}</select></label>
            <label><span class="obrigatorio">Clínica</span>
                <select name="clinicaId" required><option value="">Selecione…</option>${opcoes(clinicas.filter((c) => c.status === "ATIVO"), (c) => c.nome)}</select></label>
            <label><span class="obrigatorio">Veterinário</span>
                <select name="veterinarioId" id="ag-vet" required><option value="">Selecione…</option>${opcoes(vets.filter((v) => v.status === "ATIVO"), (v) => `${v.nome} — ${v.especialidade ?? ""}`)}</select></label>
            <label><span class="obrigatorio">Data e hora</span><input type="datetime-local" name="dataPrevista" id="ag-data" required></label>
            <label>Valor (R$)<input type="number" name="valor" step="0.01" min="0" placeholder="0,00"></label>
            <div class="inteiro" id="ag-disponibilidade"></div>
            <label class="inteiro">Sintomas / motivo<textarea name="descricaoSintomas"></textarea></label>
        </div>`,
        async (form) => {
            const dataPrevista = form.get("dataPrevista");
            const corpo = {
                responsavelId: Number(form.get("responsavelId")),
                petId: Number(form.get("petId")),
                clinicaId: Number(form.get("clinicaId")),
                veterinarioId: Number(form.get("veterinarioId")),
                dataPrevista: dataPrevista.length === 16 ? dataPrevista + ":00" : dataPrevista,
                tipoConsulta: form.get("tipoConsulta"),
                descricaoSintomas: form.get("descricaoSintomas") || null,
                valor: form.get("valor") ? Number(form.get("valor")) : null
            };
            await executar(() => api("/api/consultas", { metodo: "POST", corpo }), "Consulta agendada.");
            await aoConcluir();
        }, "Agendar");

    // Pets filtrados pelo responsável escolhido
    $("#ag-tutor").addEventListener("change", (ev) => {
        const doTutor = pets.filter((p) => String(p.responsavelId) === ev.target.value);
        $("#ag-pet").innerHTML = doTutor.length
            ? `<option value="">Selecione…</option>${opcoes(doTutor, (p) => `${p.nome} (${p.especie})`)}`
            : `<option value="">Nenhum pet para este responsável</option>`;
    });

    // Vagas do veterinário no dia: função pkg_clyvo.valida_limite_agenda
    const verificar = async () => {
        const vet = $("#ag-vet").value;
        const dt = $("#ag-data").value;
        const caixa = $("#ag-disponibilidade");
        if (!vet || !dt) { caixa.innerHTML = ""; return; }
        try {
            const r = await api(`/api/consultas/disponibilidade?veterinarioId=${vet}&data=${encodeURIComponent(dt.length === 16 ? dt + ":00" : dt)}`);
            const livre = r.situacao.startsWith("DISPONIVEL");
            caixa.innerHTML = `<div class="disponibilidade etiqueta-${livre ? "ok" : "perigo"}">
                ${livre ? "✔" : "✖"} Agenda do veterinário neste dia: <strong>${esc(r.situacao)}</strong></div>`;
        } catch (e) {
            caixa.innerHTML = `<div class="disponibilidade etiqueta-perigo">${esc(e.message)}</div>`;
        }
    };
    $("#ag-vet").addEventListener("change", verificar);
    $("#ag-data").addEventListener("change", verificar);
}

// ------------------------------------------------------------------
// Relatórios e exportação
// ------------------------------------------------------------------
async function telaRelatorios() {
    conteudo.innerHTML = `
        <div class="cabecalho"><div><h1>Relatórios</h1>
            <p>Rotinas executadas no Oracle pelo package <code>pkg_clyvo</code>.</p></div></div>

        <div class="cartao">
            <h3>Faturamento por veterinário e tipo de consulta</h3>
            <div class="acoes" style="margin-bottom:14px">
                <label>De<input type="date" id="rel-inicio"></label>
                <label>Até<input type="date" id="rel-fim"></label>
                <button class="btn btn-primario" id="rel-gerar">Gerar</button>
            </div>
            <div class="tabela-wrap" id="rel-tabela">Carregando…</div>
            <p style="color:var(--cor-suave);font-size:12px;margin:10px 0 0">
                Somente consultas realizadas. Subtotais e total geral calculados manualmente pela procedure
                <code>resumo_consultas</code> (sem ROLLUP / GROUPING SETS).</p>
        </div>

        <div class="cartao">
            <h3>Exportação para o MongoDB</h3>
            <p style="margin:0 0 14px;color:var(--cor-suave)">Gera o JSON pela função <code>pkg_clyvo.exportar_colecao_json</code>,
                pronto para <code>mongoimport --jsonArray</code>.</p>
            <div class="acoes">
                <button class="btn" data-exportar="TUTORES">⬇ tutores.json</button>
                <button class="btn" data-exportar="VETERINARIOS">⬇ veterinarios.json</button>
                <button class="btn" data-exportar="CLINICAS">⬇ clinicas.json</button>
            </div>
        </div>`;

    const gerar = async () => {
        const params = new URLSearchParams();
        if ($("#rel-inicio").value) params.set("inicio", $("#rel-inicio").value);
        if ($("#rel-fim").value) params.set("fim", $("#rel-fim").value);
        try {
            const linhas = await api(`/api/relatorios/resumo-consultas?${params}`);
            $("#rel-tabela").innerHTML = linhas.length ? `<table>
                <thead><tr><th>Veterinário</th><th>Tipo de consulta</th><th class="num">Quantidade</th><th class="num">Valor</th></tr></thead>
                <tbody>${linhas.map((l) => {
                    const classe = { SUBTOTAL: "linha-subtotal", TOTAL_GERAL: "linha-total" }[l.tipoLinha] || "";
                    const vet = l.tipoLinha === "TOTAL_GERAL" ? "Total geral" : esc(l.veterinario);
                    const tipo = l.tipoLinha === "SUBTOTAL" ? "Subtotal" : l.tipoLinha === "TOTAL_GERAL" ? "" : esc(l.tipoConsulta);
                    return `<tr class="${classe}"><td>${vet}</td><td>${tipo}</td><td class="num">${l.quantidade}</td><td class="num">${moeda(l.valorTotal)}</td></tr>`;
                }).join("")}</tbody></table>` : `<p class="vazio">Nenhuma consulta realizada no período.</p>`;
        } catch (e) {
            $("#rel-tabela").innerHTML = `<p class="erro">${esc(e.message)}</p>`;
        }
    };

    $("#rel-gerar").addEventListener("click", gerar);

    conteudo.querySelectorAll("[data-exportar]").forEach((b) => b.addEventListener("click", async () => {
        const colecao = b.dataset.exportar;
        try {
            const resposta = await api(`/api/relatorios/exportacao/${colecao}`, { bruto: true });
            const blob = await resposta.blob();
            const link = document.createElement("a");
            link.href = URL.createObjectURL(blob);
            link.download = `${colecao.toLowerCase()}.json`;
            link.click();
            URL.revokeObjectURL(link.href);
            aviso(`${colecao.toLowerCase()}.json exportado.`, "ok");
        } catch (e) {
            aviso(e.message, "erro");
        }
    }));

    await gerar();
}

// ------------------------------------------------------------------
// Navegação por hash (#painel, #consultas, …)
// ------------------------------------------------------------------
function navegar() {
    if (!estado.credencial) return;
    const rota = (location.hash || "#painel").substring(1);
    const tela = telas[rota] || telas.painel;
    document.querySelectorAll("#menu a").forEach((a) => a.classList.toggle("ativo", a.dataset.rota === rota));
    tela();
}

window.addEventListener("hashchange", navegar);

// Restaura a sessão ao recarregar a página
(async () => {
    const salva = ler("clyvo.credencial");
    if (!salva) return;
    estado.credencial = salva;
    try {
        const eu = await api("/api/me");
        estado.usuario = eu.usuario;
        estado.perfis = eu.perfis;
        $("#usuario-nome").textContent = eu.usuario;
        $("#usuario-perfil").textContent = ehAdmin() ? "Administrador" : "Veterinário";
        $("#tela-login").hidden = true;
        $("#tela-app").hidden = false;
        navegar();
    } catch (e) {
        sair();
    }
})();
