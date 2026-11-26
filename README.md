. Código fuente

Archivo `MotorCifrado.scala`:

```scala
import scala.util.{Failure, Success, Try}

/**
 * Mini Proyecto: Higher Order Functions en Criptografía
 * Asignatura: Programación Funcional y Reactiva
 *
 * Motor de cifrado que recibe la estrategia de cifrado (una función)
 * como parámetro. Se implementan dos estrategias: César y XOR.
 */
object MotorCifrado {


  // 1. Función de orden superior
 
  def procesarMensaje(mensaje: String, clave: Int, cifrado: (Char, Int) => Char): Try[String] =
    Try {
      if (mensaje == null) throw new IllegalArgumentException("El mensaje es nulo.")
      if (mensaje.isEmpty) throw new IllegalArgumentException("El mensaje está vacío.")
      mensaje.map(caracter => cifrado(caracter, clave))
    }

  // 2. Estrategias de cifrado


  /** Cifrado César: E(x, k) = (pos(x) + k) mod 26. Solo letras A-Z y a-z. */
  val cesar: (Char, Int) => Char = (caracter, clave) => {
    if (clave < -25 || clave > 25)
      throw new IllegalArgumentException(s"Clave inválida para César: $clave (debe estar entre -25 y 25).")

    if (caracter >= 'A' && caracter <= 'Z')
      desplazar(caracter, 'A', clave)
    else if (caracter >= 'a' && caracter <= 'z')
      desplazar(caracter, 'a', clave)
    else
      throw new IllegalArgumentException(s"Carácter no soportado en César: '$caracter'")
  }

  // Desplaza una letra respecto a la base ('A' o 'a'); el doble módulo evita negativos.
  private def desplazar(caracter: Char, base: Char, clave: Int): Char =
    (((caracter - base + clave) % 26 + 26) % 26 + base).toChar

  /** Cifrado XOR: E(x, k) = ASCII(x) ^ k. Solo caracteres ASCII (0-127). */
  val xor: (Char, Int) => Char = (caracter, clave) => {
    if (clave < 0 || clave > 127)
      throw new IllegalArgumentException(s"Clave inválida para XOR: $clave (debe estar entre 0 y 127).")
    if (caracter.toInt > 127)
      throw new IllegalArgumentException(s"Carácter no soportado en XOR (no es ASCII): '$caracter'")

    (caracter.toInt ^ clave).toChar
  }


  // 3. Pruebas
  

  private def mostrar(titulo: String, resultado: Try[String]): Unit =
    resultado match {
      case Success(texto) => println(s"$titulo: $texto")
      case Failure(error) => println(s"$titulo -> ERROR controlado: ${error.getMessage}")
    }

  def main(args: Array[String]): Unit = {

    println("=== Caso César ===")
    val cifradoCesar = procesarMensaje("FUNCIONAL", 3, cesar)
    mostrar("Cifrado   (k = 3)", cifradoCesar)
    mostrar("Descifrado (k = -3)", cifradoCesar.flatMap(texto => procesarMensaje(texto, -3, cesar)))

    println("\n=== Caso XOR ===")
    val cifradoXor = procesarMensaje("scala", 5, xor)
    mostrar("Cifrado   (k = 5)", cifradoXor)
    mostrar("Descifrado (k = 5)", cifradoXor.flatMap(texto => procesarMensaje(texto, 5, xor)))

    println("\n=== Manejo de errores ===")
    mostrar("Mensaje vacío", procesarMensaje("", 3, cesar))
    mostrar("Mensaje nulo", procesarMensaje(null, 3, cesar))
    mostrar("César, clave muy grande (1000)", procesarMensaje("HOLA", 1000, cesar))
    mostrar("César, carácter especial ('1')", procesarMensaje("HOLA1", 3, cesar))
    mostrar("XOR, clave negativa (-1)", procesarMensaje("scala", -1, xor))
    mostrar("XOR, carácter no ASCII ('ñ')", procesarMensaje("año", 5, xor))
  }
}
```

##  Explicación

### Función de orden superior

`procesarMensaje` recibe el mensaje, la clave y **la estrategia de cifrado como función**
de tipo `(Char, Int) => Char`. Aplica la transformación a cada carácter con `map` y devuelve
un `Try[String]`: `Success` con el resultado o `Failure` con la excepción, sin detener el programa.

```scala
def procesarMensaje(mensaje: String, clave: Int, cifrado: (Char, Int) => Char): Try[String] =
  Try {
    if (mensaje == null) throw new IllegalArgumentException("El mensaje es nulo.")
    if (mensaje.isEmpty) throw new IllegalArgumentException("El mensaje está vacío.")
    mensaje.map(caracter => cifrado(caracter, clave))
  }
```

### Cifrado César

Fórmula: `E(x, k) = (pos(x) + k) mod 26`

- Soporta letras `A-Z` y `a-z`.
- Clave válida: entre `-25` y `25`.
- Se usa `((n % 26) + 26) % 26` para que las claves negativas (descifrado) no den índices negativos.

### Cifrado XOR

Fórmula: `E(x, k) = ASCII(x) ^ k`

- Usa el operador `^` dentro de la función lambda.
- Clave válida: entre `0` y `127`. Solo caracteres ASCII (código ≤ 127).

Ejemplo con la primera letra: `'s'` = 115 = `0111 0011`; `5` = `0000 0101`;
XOR = `0111 0110` = 118 = `'v'`.

## Ejecución y resultados

###  Caso César

| Operación | Entrada | Clave | Salida |
|---|---|---|---|
| Cifrar | `FUNCIONAL` | 3 | `IXQFLRQDO` |
| Descifrar | `IXQFLRQDO` | -3 | `FUNCIONAL` |

![Captura del caso César](capturas/01-cesar.png)

### Caso XOR

| Operación | Entrada | Clave | Salida |
|---|---|---|---|
| Cifrar | `scala` | 5 | `vfdid` |
| Aplicar de nuevo | `vfdid` | 5 | `scala` |

![Captura del caso XOR](capturas/02-xor.png)

###  Manejo de errores

| Prueba | Resultado |
|---|---|
| Mensaje vacío | `ERROR controlado: El mensaje está vacío.` |
| Mensaje nulo | `ERROR controlado: El mensaje es nulo.` |
| César con clave 1000 | `ERROR controlado: Clave inválida para César: 1000 (debe estar entre -25 y 25).` |
| César con `'1'` | `ERROR controlado: Carácter no soportado en César: '1'` |
| XOR con clave -1 | `ERROR controlado: Clave inválida para XOR: -1 (debe estar entre 0 y 127).` |
| XOR con `'ñ'` | `ERROR controlado: Carácter no soportado en XOR (no es ASCII): 'ñ'` |

![Captura del manejo de errores](capturas/03-errores.png)

## Preguntas de reflexión

**¿Por qué XOR es su propia función inversa, mientras que César requiere invertir el signo de la clave?**
Porque XOR cumple `(a ⊕ k) ⊕ k = a`: cada bit cambia con `k` y vuelve a su valor original al
aplicar la misma clave. En César el cifrado es una suma, y para deshacerla hay que restar
(clave `-k`, equivalente a `26 - k`).

**¿Qué ventajas ofrece `map` sobre un bucle `for` tradicional?**
`map` es declarativo: expresa *qué* transformación se aplica, no *cómo* recorrer. No
requiere variables mutables ni índices, devuelve una colección nueva (inmutabilidad) y el
código es más corto y fácil de razonar.

**¿Cómo ayuda el tipado estático a prevenir errores al pasar funciones como argumentos?**
El compilador exige que la función tenga el tipo `(Char, Int) => Char`. Si se pasa una
función con otra firma, el error aparece al compilar y no en tiempo de ejecución.

##  Conclusiones

- Pasar la estrategia de cifrado como parámetro permite agregar nuevos algoritmos sin modificar `procesarMensaje`.
- `Try` permite manejar los errores como valores, de forma controlada.
- `map` sobre colecciones inmutables hace el código más simple y seguro.
